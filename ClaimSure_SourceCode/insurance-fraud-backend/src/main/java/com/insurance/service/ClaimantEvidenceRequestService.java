package com.insurance.service;
import com.insurance.dto.EvidenceRequestResponse;
import com.insurance.dto.RepairInvoiceRequest;
import com.insurance.entity.Evidence;
import com.insurance.entity.EvidenceRequest;
import com.insurance.enums.ClaimStatus;
import com.insurance.enums.EvidenceRequestStatus;
import com.insurance.enums.EvidenceType;
import com.insurance.enums.InvestigationStatus;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.repository.ClaimRepository;
import com.insurance.repository.EvidenceRepository;
import com.insurance.repository.EvidenceRequestRepository;
import com.insurance.repository.InvestigationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.OffsetDateTime;
import java.util.List;
@Service
public class ClaimantEvidenceRequestService {
    private final EvidenceRequestRepository requestRepository;
    private final EvidenceRepository evidenceRepository;
    private final InvestigationRepository investigationRepository;
    private final ClaimRepository claimRepository;
    private final EvidenceService evidenceService;
    public ClaimantEvidenceRequestService(EvidenceRequestRepository requestRepository,
            EvidenceRepository evidenceRepository, InvestigationRepository investigationRepository,
            ClaimRepository claimRepository, EvidenceService evidenceService) {
        this.requestRepository = requestRepository;
        this.evidenceRepository = evidenceRepository;
        this.investigationRepository = investigationRepository;
        this.claimRepository = claimRepository;
        this.evidenceService = evidenceService;
    }
    @Transactional(readOnly = true)
    public List<EvidenceRequestResponse> getMyPendingRequests(Long userId) {
        return requestRepository.findByInvestigation_Claim_SubmittedBy_IdAndStatusOrderByRequestedAtDesc(userId,
                EvidenceRequestStatus.PENDING).stream().map(this::toResponse).toList();
    }
    @Transactional
    public EvidenceRequestResponse submitEvidence(Long requestId, Long userId, MultipartFile file,
            EvidenceType evidenceType, String description, RepairInvoiceRequest invoiceDetails) throws Exception {
        EvidenceRequest request = requestRepository.findByIdAndInvestigation_Claim_SubmittedBy_Id(requestId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence request not found"));
        if (request.getStatus() != EvidenceRequestStatus.PENDING) {
            throw new IllegalArgumentException("This evidence request is no longer pending");
        }
        var investigation = request.getInvestigation();
        if (investigation.getStatus() == InvestigationStatus.COMPLETED) {
            throw new IllegalArgumentException("The investigation is already completed");
        }
        var claim = investigation.getClaim();
        Evidence evidence = evidenceService.upload(claim.getId(), userId, file, evidenceType,
                description == null || description.isBlank() ? request.getDescription() : description, invoiceDetails);
        evidence.setEvidenceRequest(request);
        evidenceRepository.save(evidence);
        request.setStatus(EvidenceRequestStatus.FULFILLED);
        request.setFulfilledAt(OffsetDateTime.now());
        requestRepository.saveAndFlush(request);
        long remainingPendingRequests = requestRepository.countByInvestigation_IdAndStatus(investigation.getId(),
                EvidenceRequestStatus.PENDING);
        if (remainingPendingRequests == 0) {
            investigation.setStatus(InvestigationStatus.OPEN);
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        } else {
            investigation.setStatus(InvestigationStatus.WAITING_FOR_EVIDENCE);
            claim.setStatus(ClaimStatus.ADDITIONAL_EVIDENCE_REQUESTED);
        }
        claimRepository.save(claim);
        investigationRepository.save(investigation);
        return toResponse(request);
    }
    private EvidenceRequestResponse toResponse(EvidenceRequest request) {
        return new EvidenceRequestResponse(request.getId(), request.getInvestigation().getClaim().getId(),
                request.getInvestigation().getClaim().getClaimNumber(), request.getDescription(), request.getStatus(),
                request.getRequestedAt(), request.getDueAt(), request.getFulfilledAt());
    }
}
