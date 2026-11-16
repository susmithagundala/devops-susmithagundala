package com.insurance.controller;
import com.insurance.dto.EvidenceRequestResponse;
import com.insurance.dto.RepairInvoiceRequest;
import com.insurance.enums.EvidenceType;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.ClaimantEvidenceRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/evidence-requests")
@PreAuthorize("hasRole('CLAIMANT')")
public class ClaimantEvidenceRequestController {
    private final ClaimantEvidenceRequestService service;
    public ClaimantEvidenceRequestController(ClaimantEvidenceRequestService service) {
        this.service = service;
    }
    @GetMapping
    public List<EvidenceRequestResponse> myPendingRequests(@AuthenticationPrincipal CustomUserDetails user) {
        return service.getMyPendingRequests(user.getId());
    }
    @PostMapping(value = "/{requestId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceRequestResponse> submitEvidence(@PathVariable Long requestId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "OTHER") EvidenceType evidenceType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String repairerName,
            @RequestParam(required = false) String repairerGstin,
            @RequestParam(required = false) String repairerPhone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate invoiceDate,
            @RequestParam(required = false) BigDecimal invoiceAmount,
            @AuthenticationPrincipal CustomUserDetails user) throws Exception {
        RepairInvoiceRequest invoiceDetails = null;
        if (evidenceType == EvidenceType.REPAIR_INVOICE) {
            invoiceDetails = new RepairInvoiceRequest(invoiceNumber, repairerName, repairerGstin, repairerPhone,
                    invoiceDate, invoiceAmount);
        }
        EvidenceRequestResponse response = service.submitEvidence(requestId, user.getId(), file, evidenceType,
                description, invoiceDetails);
        return ResponseEntity.ok(response);
    }
}
