package com.insurance.dto;
import com.insurance.enums.ClaimStatus;
import com.insurance.enums.RiskBand;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
public record ClaimSummaryResponse(Long id, String claimNumber, Long policyId, Long vehicleId, LocalDate incidentDate,
        String incidentLocation, String incidentDescription, BigDecimal claimedAmount, Integer riskScore,
        RiskBand riskBand, ClaimStatus status, OffsetDateTime submittedAt) {
}
