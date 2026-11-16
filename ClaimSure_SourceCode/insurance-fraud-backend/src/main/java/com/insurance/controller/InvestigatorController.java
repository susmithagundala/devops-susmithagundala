package com.insurance.controller;
import com.insurance.dto.InvestigatorResponses.*;
import com.insurance.entity.*;
import com.insurance.enums.DecisionType;
import com.insurance.enums.ClaimStatus;
import com.insurance.repository.*;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.InvestigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/investigator")
@PreAuthorize("hasRole('INVESTIGATOR')")
public class InvestigatorController {
    private final InvestigationService investigationService;
    private final InvestigationRepository investigationRepository;
    private final InvestigationNoteRepository noteRepository;
    private final EvidenceRequestRepository evidenceRequestRepository;
    private final EntityMatchRepository entityMatchRepository;
    private final ClaimFlagRepository claimFlagRepository;
    public InvestigatorController(InvestigationService investigationService,
            InvestigationRepository investigationRepository, InvestigationNoteRepository noteRepository,
            EvidenceRequestRepository evidenceRequestRepository, EntityMatchRepository entityMatchRepository,
            ClaimFlagRepository claimFlagRepository) {
        this.investigationService = investigationService;
        this.investigationRepository = investigationRepository;
        this.noteRepository = noteRepository;
        this.evidenceRequestRepository = evidenceRequestRepository;
        this.entityMatchRepository = entityMatchRepository;
        this.claimFlagRepository = claimFlagRepository;
    }
    @GetMapping("/investigations")
    @Transactional(readOnly = true)
    public List<InvestigationSummary> myInvestigations(@AuthenticationPrincipal CustomUserDetails user) {
        return investigationRepository.findByInvestigator_IdOrderByStartedAtDesc(user.getId()).stream()
                .map(this::toInvestigationSummary).toList();
    }
    @GetMapping("/claims/flagged")
    @Transactional(readOnly = true)
    public List<FlaggedClaimSummary> flaggedClaims() {
        return claimFlagRepository.findByClaim_StatusOrderByClaim_RiskScoreDesc(ClaimStatus.UNDER_REVIEW).stream()
                .collect(java.util.stream.Collectors.toMap(flag -> flag.getClaim().getId(), flag -> flag.getClaim(),
                        (first, second) -> first, java.util.LinkedHashMap::new))
                .values().stream().filter(claim -> !investigationRepository.existsByClaim_Id(claim.getId()))
                .map(claim -> {
                    ClaimSummary summary = new ClaimSummary(claim.getId(), claim.getClaimNumber(),
                            claim.getIncidentDate(), claim.getIncidentLocation(), claim.getClaimedAmount(),
                            claim.getRiskScore(), claim.getRiskBand(), claim.getStatus());
                    List<ClaimFlagSummary> flags = claimFlagRepository.findByClaim_IdOrderByCreatedAtAsc(claim.getId())
                            .stream().map(flag -> new ClaimFlagSummary(flag.getId(), flag.getRuleCodeSnapshot(),
                                    flag.getScoreContribution(), flag.getReason()))
                            .toList();
                    return new FlaggedClaimSummary(summary, flags);
                }).toList();
    }
    @GetMapping("/investigations/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<InvestigationSummary> getInvestigation(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        Investigation investigation = findAssignedInvestigation(id, user.getId());
        return ResponseEntity.ok(toInvestigationSummary(investigation));
    }
    @PostMapping("/claims/{claimId}/open")
    @Transactional
    public ResponseEntity<InvestigationSummary> open(@PathVariable Long claimId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Investigation investigation = investigationService.openInvestigation(claimId, user.getId());
        return ResponseEntity.ok(toInvestigationSummary(investigation));
    }
    @GetMapping("/investigations/{id}/notes")
    @Transactional(readOnly = true)
    public List<NoteSummary> notes(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        requireAssignment(id, user.getId());
        return noteRepository.findByInvestigation_IdOrderByCreatedAtAsc(id).stream().map(this::toNoteSummary).toList();
    }
    @PostMapping("/investigations/{id}/notes")
    @Transactional
    public ResponseEntity<NoteSummary> addNote(@PathVariable Long id, @RequestBody NoteRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        InvestigationNote note = investigationService.addNote(id, user.getId(), request.note());
        return ResponseEntity.status(201).body(toNoteSummary(note));
    }
    @PostMapping("/investigations/{id}/evidence-requests")
    @Transactional
    public ResponseEntity<EvidenceRequestSummary> requestEvidence(@PathVariable Long id,
            @RequestBody EvidenceRequestBody request, @AuthenticationPrincipal CustomUserDetails user) {
        EvidenceRequest evidenceRequest = investigationService.requestEvidence(id, user.getId(), request.description());
        return ResponseEntity.status(201).body(toEvidenceRequestSummary(evidenceRequest));
    }
    @GetMapping("/investigations/{id}/evidence-requests")
    @Transactional(readOnly = true)
    public List<EvidenceRequestSummary> evidenceRequests(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        requireAssignment(id, user.getId());
        return evidenceRequestRepository.findByInvestigation_IdOrderByRequestedAtDesc(id).stream()
                .map(this::toEvidenceRequestSummary).toList();
    }
    @PostMapping("/investigations/{id}/decision")
    @Transactional
    public ResponseEntity<DecisionSummary> decide(@PathVariable Long id, @RequestBody DecisionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        ClaimDecision decision = investigationService.decide(id, user.getId(), request.decision(), request.reason());
        return ResponseEntity.ok(toDecisionSummary(decision));
    }
    @GetMapping("/claims/{claimId}/matches")
    @Transactional(readOnly = true)
    public List<EntityMatchSummary> matches(@PathVariable Long claimId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Investigation investigation = investigationRepository.findByClaim_Id(claimId)
                .orElseThrow(() -> new com.insurance.exception.ResourceNotFoundException("Investigation not found"));
        requireAssignment(investigation.getId(), user.getId());
        return entityMatchRepository.findByClaim_Id(claimId).stream().map(this::toEntityMatchSummary).toList();
    }
    private Investigation findAssignedInvestigation(Long id, Long userId) {
        Investigation investigation = investigationRepository.findById(id)
                .orElseThrow(() -> new com.insurance.exception.ResourceNotFoundException("Investigation not found"));
        if (investigation.getInvestigator() == null || !investigation.getInvestigator().getId().equals(userId)) {
            throw new com.insurance.exception.UnauthorizedAccessException(
                    "Investigation assigned to another investigator");
        }
        return investigation;
    }
    private void requireAssignment(Long id, Long userId) {
        findAssignedInvestigation(id, userId);
    }
    private InvestigationSummary toInvestigationSummary(Investigation investigation) {
        Claim claim = investigation.getClaim();
        ClaimSummary claimSummary = new ClaimSummary(claim.getId(), claim.getClaimNumber(), claim.getIncidentDate(),
                claim.getIncidentLocation(), claim.getClaimedAmount(), claim.getRiskScore(), claim.getRiskBand(),
                claim.getStatus());
        return new InvestigationSummary(investigation.getId(), claimSummary,
                toUserSummary(investigation.getInvestigator()), investigation.getStatus(), investigation.getStartedAt(),
                investigation.getCompletedAt());
    }
    private NoteSummary toNoteSummary(InvestigationNote note) {
        return new NoteSummary(note.getId(), toUserSummary(note.getAuthor()), note.getNote(), note.getCreatedAt());
    }
    private EvidenceRequestSummary toEvidenceRequestSummary(EvidenceRequest request) {
        return new EvidenceRequestSummary(request.getId(), request.getDescription(), request.getStatus(),
                request.getRequestedAt(), request.getDueAt(), request.getFulfilledAt());
    }
    private DecisionSummary toDecisionSummary(ClaimDecision decision) {
        return new DecisionSummary(decision.getId(), decision.getClaim().getId(), decision.getDecision(),
                decision.getReason(), decision.getDecidedAt(), toUserSummary(decision.getDecidedBy()));
    }
    private EntityMatchSummary toEntityMatchSummary(EntityMatch match) {
        return new EntityMatchSummary(match.getId(), match.getMatchType(), match.getConfidenceScore(),
                match.getMatchDetails(), match.getReviewStatus());
    }
    private UserSummary toUserSummary(AppUser user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(user.getId(), user.getEmail());
    }
    public record NoteRequest(String note) {
    }
    public record EvidenceRequestBody(String description) {
    }
    public record DecisionRequest(DecisionType decision, String reason) {
    }
}
