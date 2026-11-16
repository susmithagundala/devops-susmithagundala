package com.insurance.service;

import com.insurance.dto.RepairInvoiceRequest;
import com.insurance.entity.AppUser;
import com.insurance.entity.Claim;
import com.insurance.entity.Evidence;
import com.insurance.entity.Investigation;
import com.insurance.entity.RepairInvoice;
import com.insurance.enums.EvidenceType;
import com.insurance.enums.RoleName;
import com.insurance.enums.StorageProvider;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.exception.UnauthorizedAccessException;
import com.insurance.repository.AppUserRepository;
import com.insurance.repository.ClaimRepository;
import com.insurance.repository.EvidenceRepository;
import com.insurance.repository.InvestigationRepository;
import com.insurance.repository.RepairInvoiceRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class EvidenceService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final EvidenceRepository evidenceRepository;
    private final ClaimRepository claimRepository;
    private final AppUserRepository userRepository;
    private final InvestigationRepository investigationRepository;
    private final RepairInvoiceRepository invoiceRepository;
    private final S3StorageService storageService;
    private final AuditService auditService;
    private final EntityMatchingService entityMatchingService;
    private final FraudDetectionService fraudDetectionService;

    @Value("${app.aws.s3.bucket:}")
    private String bucket;

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            ClaimRepository claimRepository,
            AppUserRepository userRepository,
            InvestigationRepository investigationRepository,
            RepairInvoiceRepository invoiceRepository,
            S3StorageService storageService,
            AuditService auditService,
            EntityMatchingService entityMatchingService,
            FraudDetectionService fraudDetectionService
    ) {
        this.evidenceRepository = evidenceRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.investigationRepository = investigationRepository;
        this.invoiceRepository = invoiceRepository;
        this.storageService = storageService;
        this.auditService = auditService;
        this.entityMatchingService = entityMatchingService;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Transactional
    public Evidence upload(
            Long claimId,
            Long userId,
            MultipartFile file,
            EvidenceType evidenceType,
            String description
    ) throws Exception {

        return upload(
                claimId,
                userId,
                file,
                evidenceType,
                description,
                null
        );
    }

    @Transactional
    public Evidence upload(
            Long claimId,
            Long userId,
            MultipartFile file,
            EvidenceType evidenceType,
            String description,
            RepairInvoiceRequest invoiceDetails
    ) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a file"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Maximum allowed file size is 10 MB"
            );
        }

        String contentType = file.getContentType();

        if (!isAllowedContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Only PDF, JPEG, PNG and WebP files are allowed"
            );
        }

        EvidenceType selectedType =
                evidenceType == null
                        ? EvidenceType.OTHER
                        : evidenceType;

        if (selectedType == EvidenceType.REPAIR_INVOICE) {
            validateInvoiceDetails(invoiceDetails);
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Claim not found")
                );

        verifyClaimEvidenceAccess(claim, user);

        String checksum = sha256(file.getBytes());

        String originalName =
                sanitizeFileName(file.getOriginalFilename());

        String objectKey = "claims/" + claimId + "/"
                + UUID.randomUUID() + "-" + originalName;

        storageService.upload(
                objectKey,
                file.getInputStream(),
                file.getSize(),
                contentType
        );

        try {
            Evidence evidence = new Evidence();

            evidence.setClaim(claim);
            evidence.setUploadedBy(user);
            evidence.setFileName(originalName);
            evidence.setContentType(contentType);
            evidence.setFileSize(file.getSize());
            evidence.setStorageProvider(StorageProvider.S3);
            evidence.setS3Bucket(bucket);
            evidence.setS3ObjectKey(objectKey);
            evidence.setSha256Checksum(checksum);
            evidence.setEvidenceType(selectedType);
            evidence.setDescription(description);

            Evidence savedEvidence =
                    evidenceRepository.saveAndFlush(evidence);

            if (selectedType == EvidenceType.REPAIR_INVOICE) {

                RepairInvoice invoice = new RepairInvoice();

                invoice.setEvidence(savedEvidence);
                invoice.setInvoiceNumber(
                        invoiceDetails.invoiceNumber().trim()
                );
                invoice.setRepairerName(
                        invoiceDetails.repairerName().trim()
                );
                invoice.setRepairerGstin(
                        normalizeGstin(invoiceDetails.repairerGstin())
                );
                invoice.setRepairerPhone(
                        normalizePhone(invoiceDetails.repairerPhone())
                );
                invoice.setInvoiceDate(invoiceDetails.invoiceDate());
                invoice.setInvoiceAmount(invoiceDetails.invoiceAmount());
                invoice.setCurrency("INR");

                RepairInvoice savedInvoice =
                        invoiceRepository.saveAndFlush(invoice);

                entityMatchingService.evaluateInvoiceMatches(
                        claim,
                        savedInvoice
                );

                fraudDetectionService.evaluate(claim);
            }

            auditService.record(
                    user,
                    "EVIDENCE_UPLOADED",
                    "EVIDENCE",
                    savedEvidence.getId(),
                    Map.of(
                            "claimId", claimId,
                            "fileName", originalName,
                            "evidenceType", selectedType.name()
                    ),
                    null
            );

            return savedEvidence;

        } catch (Exception ex) {

            try {
                storageService.delete(objectKey);
            } catch (Exception cleanupException) {
                ex.addSuppressed(cleanupException);
            }

            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<Evidence> getClaimEvidence(
            Long claimId,
            Long userId
    ) {

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Claim not found")
                );

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        verifyClaimEvidenceAccess(claim, user);

        return evidenceRepository
                .findByClaim_IdOrderByUploadedAtDesc(claimId);
    }

    @Transactional(readOnly = true)
    public Evidence getEvidence(
            Long evidenceId,
            Long userId
    ) {

        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evidence not found")
                );

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        verifyClaimEvidenceAccess(evidence.getClaim(), user);

        return evidence;
    }

    private void verifyClaimEvidenceAccess(
            Claim claim,
            AppUser user
    ) {

        Long userId = user.getId();

        boolean owner = claim.getSubmittedBy()
                .getId()
                .equals(userId);

        boolean investigator = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName() == RoleName.INVESTIGATOR
                );

        boolean assignedInvestigator = investigator
                && investigationRepository.findByClaim_Id(claim.getId())
                        .map(Investigation::getInvestigator)
                        .map(AppUser::getId)
                        .map(assignedUserId ->
                                assignedUserId.equals(userId)
                        )
                        .orElse(false);

        if (!owner && !assignedInvestigator) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to access evidence for this claim"
            );
        }
    }

    private void validateInvoiceDetails(
            RepairInvoiceRequest invoice
    ) {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "Invoice details are required for repair invoice evidence"
            );
        }

        if (invoice.invoiceNumber() == null
                || invoice.invoiceNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Invoice number is required"
            );
        }

        if (invoice.repairerName() == null
                || invoice.repairerName().isBlank()) {
            throw new IllegalArgumentException(
                    "Repairer name is required"
            );
        }

        if (invoice.invoiceDate() == null
                || invoice.invoiceDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "A valid invoice date is required"
            );
        }

        if (invoice.invoiceAmount() == null
                || invoice.invoiceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Invoice amount must be greater than zero"
            );
        }

        if (invoice.repairerGstin() != null
                && !invoice.repairerGstin().isBlank()
                && !invoice.repairerGstin().trim()
                        .matches("[0-9A-Za-z]{15}")) {
            throw new IllegalArgumentException(
                    "Repairer GSTIN must contain 15 alphanumeric characters"
            );
        }
    }

    private String normalizeGstin(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePhone(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.replaceAll("[^0-9+]", "");
    }

    public boolean isAllowedContentType(String contentType) {

        return contentType != null
                && (
                        contentType.equalsIgnoreCase("application/pdf")
                        || contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/png")
                        || contentType.equalsIgnoreCase("image/webp")
                );
    }

    private String sanitizeFileName(String name) {

        if (name == null || name.isBlank()) {
            return "evidence";
        }

        String cleaned = name.replace("\\", "/");

        cleaned = cleaned.substring(
                cleaned.lastIndexOf('/') + 1
        );

        cleaned = cleaned.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );

        return cleaned.length() > 150
                ? cleaned.substring(cleaned.length() - 150)
                : cleaned;
    }

    private String sha256(byte[] bytes) throws Exception {

        byte[] digest = MessageDigest
                .getInstance("SHA-256")
                .digest(bytes);

        return HexFormat.of().formatHex(digest);
    }
}
