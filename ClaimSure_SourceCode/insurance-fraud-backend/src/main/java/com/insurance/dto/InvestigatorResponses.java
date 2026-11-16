package com.insurance.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.insurance.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class InvestigatorResponses {

    private InvestigatorResponses() {
    }

    public record UserSummary(Long id, String email) {
    }

    public record ClaimSummary(
            Long id,
            String claimNumber,
            LocalDate incidentDate,
            String incidentLocation,
            BigDecimal claimedAmount,
            Integer riskScore,
            RiskBand riskBand,
            ClaimStatus status
    ) {
    }

    public record InvestigationSummary(
            Long id,
            ClaimSummary claim,
            UserSummary investigator,
            InvestigationStatus status,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt
    ) {
    }

    public record NoteSummary(
            Long id,
            UserSummary author,
            String note,
            OffsetDateTime createdAt
    ) {
    }

    public record EvidenceRequestSummary(
            Long id,
            String description,
            EvidenceRequestStatus status,
            OffsetDateTime requestedAt,
            OffsetDateTime dueAt,
            OffsetDateTime fulfilledAt
    ) {
    }

    public record DecisionSummary(
            Long id,
            Long claimId,
            DecisionType decision,
            String reason,
            OffsetDateTime decidedAt,
            UserSummary decidedBy
    ) {
    }

    public record EntityMatchSummary(
            Long id,
            EntityMatchType matchType,
            BigDecimal confidenceScore,
            JsonNode matchDetails,
            EntityMatchReviewStatus reviewStatus
    ) {
    }

    public record ClaimFlagSummary(
            Long id,
            String ruleCode,
            Integer scoreContribution,
            String reason
    ) {
    }

    public record FlaggedClaimSummary(
            ClaimSummary claim,
            java.util.List<ClaimFlagSummary> flags
    ) {
    }
}
