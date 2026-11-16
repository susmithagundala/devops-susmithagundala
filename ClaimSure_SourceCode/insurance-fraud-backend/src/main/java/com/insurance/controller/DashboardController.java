package com.insurance.controller;
import com.insurance.enums.ClaimStatus;
import com.insurance.enums.RiskBand;
import com.insurance.repository.ClaimRepository;
import com.insurance.repository.EvidenceRequestRepository;
import com.insurance.repository.InvestigationRepository;
import com.insurance.security.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('INVESTIGATOR')")
public class DashboardController {
    private final ClaimRepository claimRepository;
    private final InvestigationRepository investigationRepository;
    private final EvidenceRequestRepository evidenceRequestRepository;
    public DashboardController(ClaimRepository claimRepository, InvestigationRepository investigationRepository,
            EvidenceRequestRepository evidenceRequestRepository) {
        this.claimRepository = claimRepository;
        this.investigationRepository = investigationRepository;
        this.evidenceRequestRepository = evidenceRequestRepository;
    }
    @GetMapping("/investigator")
    public Map<String, Object> investigatorDashboard(@AuthenticationPrincipal CustomUserDetails user) {
        Long investigatorId = user.getId();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInvestigations",
                investigationRepository.findByInvestigator_IdOrderByStartedAtDesc(investigatorId).size());
        result.put("openInvestigations",
                investigationRepository.findByInvestigator_IdAndStatusOrderByStartedAtAsc(investigatorId,
                        com.insurance.enums.InvestigationStatus.OPEN).size());
        result.put("waitingForEvidence",
                investigationRepository.findByInvestigator_IdAndStatusOrderByStartedAtAsc(investigatorId,
                        com.insurance.enums.InvestigationStatus.WAITING_FOR_EVIDENCE).size());
        result.put("highRiskClaims", claimRepository.findByRiskBandOrderByRiskScoreDesc(RiskBand.HIGH).size());
        result.put("claimsUnderReview",
                claimRepository.findByStatusOrderByCreatedAtAsc(ClaimStatus.UNDER_REVIEW).size());
        result.put("additionalEvidenceRequested",
                claimRepository.findByStatusOrderByCreatedAtAsc(ClaimStatus.ADDITIONAL_EVIDENCE_REQUESTED).size());
        return result;
    }
}
