package com.insurance.controller;

import com.insurance.dto.RepairInvoiceRequest;
import com.insurance.entity.Evidence;
import com.insurance.enums.EvidenceType;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.EvidenceService;
import com.insurance.service.S3StorageService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final S3StorageService storageService;

    public EvidenceController(
            EvidenceService evidenceService,
            S3StorageService storageService
    ) {
        this.evidenceService = evidenceService;
        this.storageService = storageService;
    }

    @PostMapping(
            value = "/claims/{claimId}/evidence",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> upload(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "OTHER")
            EvidenceType evidenceType,
            @RequestParam(required = false)
            String description,
            @RequestParam(required = false)
            String invoiceNumber,
            @RequestParam(required = false)
            String repairerName,
            @RequestParam(required = false)
            String repairerGstin,
            @RequestParam(required = false)
            String repairerPhone,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate invoiceDate,
            @RequestParam(required = false)
            BigDecimal invoiceAmount,
            @AuthenticationPrincipal
            CustomUserDetails user
    ) throws Exception {

        RepairInvoiceRequest invoiceDetails = null;

        if (evidenceType == EvidenceType.REPAIR_INVOICE) {

            invoiceDetails = new RepairInvoiceRequest(
                    invoiceNumber,
                    repairerName,
                    repairerGstin,
                    repairerPhone,
                    invoiceDate,
                    invoiceAmount
            );
        }

        Evidence evidence = evidenceService.upload(
                claimId,
                user.getId(),
                file,
                evidenceType,
                description,
                invoiceDetails
        );

        return ResponseEntity.status(201)
                .body(toEvidenceResponse(evidence));
    }

    @GetMapping("/claims/{claimId}/evidence")
    public ResponseEntity<List<EvidenceResponse>> getClaimEvidence(
            @PathVariable Long claimId,
            @AuthenticationPrincipal
            CustomUserDetails user
    ) {

        List<EvidenceResponse> response =
                evidenceService.getClaimEvidence(
                        claimId,
                        user.getId()
                )
                .stream()
                .map(this::toEvidenceResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/evidence/{evidenceId}")
    public ResponseEntity<?> metadata(
            @PathVariable Long evidenceId,
            @AuthenticationPrincipal
            CustomUserDetails user
    ) {

        Evidence evidence = evidenceService.getEvidence(
                evidenceId,
                user.getId()
        );

        return ResponseEntity.ok(toEvidenceResponse(evidence));
    }

    @GetMapping("/evidence/{evidenceId}/download")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable Long evidenceId,
            @AuthenticationPrincipal
            CustomUserDetails user
    ) {

        Evidence evidence = evidenceService.getEvidence(
                evidenceId,
                user.getId()
        );

        if (evidence.getS3ObjectKey() == null
                || evidence.getS3ObjectKey().isBlank()) {

            throw new IllegalStateException(
                    "Evidence does not have an associated S3 object"
            );
        }

        var stream = storageService.download(
                evidence.getS3ObjectKey()
        );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                evidence.getContentType()
                        )
                )
                .contentLength(evidence.getFileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + evidence.getFileName().replace("\"", "")
                                + "\""
                )
                .body(new InputStreamResource(stream));
    }

    private EvidenceResponse toEvidenceResponse(
            Evidence evidence
    ) {

        return new EvidenceResponse(
                evidence.getId(),
                evidence.getClaim().getId(),
                evidence.getFileName(),
                evidence.getContentType(),
                evidence.getFileSize(),
                evidence.getEvidenceType().name(),
                evidence.getSha256Checksum(),
                evidence.getUploadedAt()
        );
    }

    public record EvidenceResponse(
            Long id,
            Long claimId,
            String fileName,
            String contentType,
            Long fileSize,
            String evidenceType,
            String sha256Checksum,
            java.time.OffsetDateTime uploadedAt
    ) {
    }
}
