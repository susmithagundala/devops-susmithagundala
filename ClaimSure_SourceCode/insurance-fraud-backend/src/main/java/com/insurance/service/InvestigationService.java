package com.insurance.service;
import com.insurance.entity.*;
import com.insurance.enums.*;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Map;
@Service
public class InvestigationService {
    private final ClaimRepository claimRepository;
    private final AppUserRepository userRepository;
    private final InvestigationRepository investigationRepository;
    private final InvestigationNoteRepository noteRepository;
    private final EvidenceRequestRepository evidenceRequestRepository;
    private final ClaimDecisionRepository decisionRepository;
    private final AuditService auditService;
    public InvestigationService(ClaimRepository claimRepository, AppUserRepository userRepository,
            InvestigationRepository investigationRepository, InvestigationNoteRepository noteRepository,
            EvidenceRequestRepository evidenceRequestRepository, ClaimDecisionRepository decisionRepository,
            AuditService auditService) {
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.investigationRepository = investigationRepository;
        this.noteRepository = noteRepository;
        this.evidenceRequestRepository = evidenceRequestRepository;
        this.decisionRepository = decisionRepository;
        this.auditService = auditService;
    }
    @Transactional
    public Investigation openInvestigation(Long claimId, Long investigatorId) {
        Claim claim = findClaim(claimId);
        AppUser investigator = findUser(investigatorId);
        requireInvestigator(investigator);
        if (investigationRepository.existsByClaim_Id(claimId)) {
            Investigation existing = investigationRepository.findByClaim_Id(claimId).orElseThrow();
            requireAssignedInvestigator(existing, investigatorId);
            return existing;
        }
        if (claim.getStatus() == ClaimStatus.CLEARED_FOR_PROCESSING || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new IllegalArgumentException("A completed claim cannot be opened for investigation");
        }
        Investigation investigation = new Investigation();
        investigation.setClaim(claim);
        investigation.setInvestigator(investigator);
        investigation.setStatus(InvestigationStatus.OPEN);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claimRepository.save(claim);
        Investigation saved = investigationRepository.save(investigation);
        auditService.record(investigator, "INVESTIGATION_OPENED", "CLAIM", claimId,
                Map.of("investigationId", saved.getId()), null);
        return saved;
    }
    @Transactional
    public InvestigationNote addNote(Long investigationId, Long investigatorId, String noteText) {
        if (noteText == null || noteText.isBlank()) {
            throw new IllegalArgumentException("Investigation note cannot be empty");
        }
        Investigation investigation = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation not found"));
        AppUser investigator = findUser(investigatorId);
        requireInvestigator(investigator);
        requireAssignedInvestigator(investigation, investigatorId);
        if (investigation.getStatus() == InvestigationStatus.COMPLETED) {
            throw new IllegalArgumentException("This investigation is already completed");
        }
        InvestigationNote note = new InvestigationNote();
        note.setInvestigation(investigation);
        note.setAuthor(investigator);
        note.setNote(noteText.trim());
        InvestigationNote saved = noteRepository.save(note);
        auditService.record(investigator, "INVESTIGATION_NOTE_ADDED", "INVESTIGATION", investigationId,
                Map.of("noteId", saved.getId()), null);
        return saved;
    }
    @Transactional
    public EvidenceRequest requestEvidence(Long investigationId, Long investigatorId, String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Evidence request description is required");
        }
        Investigation investigation = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation not found"));
        AppUser investigator = findUser(investigatorId);
        requireInvestigator(investigator);
        requireAssignedInvestigator(investigation, investigatorId);
        if (investigation.getStatus() == InvestigationStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot request evidence for a completed investigation");
        }
        EvidenceRequest request = new EvidenceRequest();
        request.setInvestigation(investigation);
        request.setRequestedBy(investigator);
        request.setDescription(description.trim());
        request.setStatus(EvidenceRequestStatus.PENDING);
        request.setDueAt(OffsetDateTime.now().plusDays(7));
        investigation.setStatus(InvestigationStatus.WAITING_FOR_EVIDENCE);
        investigation.getClaim().setStatus(ClaimStatus.ADDITIONAL_EVIDENCE_REQUESTED);
        claimRepository.save(investigation.getClaim());
        EvidenceRequest saved = evidenceRequestRepository.save(request);
        auditService.record(investigator, "EVIDENCE_REQUESTED", "CLAIM", investigation.getClaim().getId(),
                Map.of("evidenceRequestId", saved.getId()), null);
        return saved;
    }
    @Transactional
    public ClaimDecision decide(Long investigationId, Long investigatorId, DecisionType decisionType, String reason) {
        Investigation investigation = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation not found"));
        AppUser investigator = findUser(investigatorId);
        requireInvestigator(investigator);
        requireAssignedInvestigator(investigation, investigatorId);
        if (investigation.getStatus() == InvestigationStatus.COMPLETED) {
            throw new IllegalArgumentException("This investigation is already completed");
        }
        long pendingEvidenceRequests = evidenceRequestRepository.countByInvestigation_IdAndStatus(investigationId,
                EvidenceRequestStatus.PENDING);
        if (pendingEvidenceRequests > 0) {
            throw new IllegalArgumentException("Resolve all pending evidence requests before deciding this claim");
        }
        if (decisionRepository.existsByClaim_Id(investigation.getClaim().getId())) {
            throw new IllegalArgumentException("A decision already exists for this claim");
        }
        if (decisionType == DecisionType.REJECTED && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("A rejection reason is mandatory");
        }
        Claim claim = investigation.getClaim();
        ClaimDecision decision = new ClaimDecision();
        decision.setClaim(claim);
        decision.setInvestigation(investigation);
        decision.setDecidedBy(investigator);
        decision.setDecision(decisionType);
        decision.setReason(reason == null || reason.isBlank() ? null : reason.trim());
        claim.setStatus(
                decisionType == DecisionType.REJECTED ? ClaimStatus.REJECTED : ClaimStatus.CLEARED_FOR_PROCESSING);
        investigation.setStatus(InvestigationStatus.COMPLETED);
        investigation.setCompletedAt(OffsetDateTime.now());
        ClaimDecision saved = decisionRepository.save(decision);
        claimRepository.save(claim);
        investigationRepository.save(investigation);
        auditService.record(investigator, "CLAIM_DECISION_RECORDED", "CLAIM", claim.getId(),
                Map.of("decision", decisionType.name(), "reason", reason == null ? "" : reason), null);
        return saved;
    }
    private Claim findClaim(Long id) {
        return claimRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Claim not found"));
    }
    private AppUser findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    private void requireInvestigator(AppUser user) {
        boolean allowed = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.INVESTIGATOR);
        if (!allowed) {
            throw new com.insurance.exception.UnauthorizedAccessException(
                    "Only investigators can perform this operation");
        }
    }
    private void requireAssignedInvestigator(Investigation investigation, Long userId) {
        if (!investigation.getInvestigator().getId().equals(userId)) {
            throw new com.insurance.exception.UnauthorizedAccessException(
                    "This investigation is assigned to another investigator");
        }
    }
}
