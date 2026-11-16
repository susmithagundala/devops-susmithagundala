package com.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.entity.Claim;
import com.insurance.entity.ClaimFlag;
import com.insurance.entity.EntityMatch;
import com.insurance.entity.FraudRuleConfiguration;
import com.insurance.entity.Vehicle;
import com.insurance.enums.ClaimStatus;
import com.insurance.enums.EntityMatchReviewStatus;
import com.insurance.enums.EntityMatchType;
import com.insurance.enums.RiskBand;
import com.insurance.repository.ClaimFlagRepository;
import com.insurance.repository.ClaimRepository;
import com.insurance.repository.EntityMatchRepository;
import com.insurance.repository.FraudRuleConfigurationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FraudDetectionService {

    private final FraudRuleConfigurationRepository ruleRepository;
    private final ClaimFlagRepository flagRepository;
    private final EntityMatchRepository matchRepository;
    private final ClaimRepository claimRepository;
    private final ObjectMapper objectMapper;

    public FraudDetectionService(
            FraudRuleConfigurationRepository ruleRepository,
            ClaimFlagRepository flagRepository,
            EntityMatchRepository matchRepository,
            ClaimRepository claimRepository,
            ObjectMapper objectMapper
    ) {
        this.ruleRepository = ruleRepository;
        this.flagRepository = flagRepository;
        this.matchRepository = matchRepository;
        this.claimRepository = claimRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EvaluationResult evaluate(Claim claim) {

        List<RuleHit> hits = new ArrayList<>();

        Long customerId = claim.getCustomer().getId();
        Long policyId = claim.getPolicyVehicle()
                .getPolicy()
                .getId();
        Long vehicleId = claim.getPolicyVehicle()
                .getVehicle()
                .getId();

        LocalDate incidentDate = claim.getIncidentDate();

        List<Claim> sameVehicleAndDate =
                claimRepository.findByPolicyVehicle_Vehicle_IdAndIncidentDate(
                        vehicleId,
                        incidentDate
                );

        boolean exactDuplicateFound = false;

        for (Claim other : sameVehicleAndDate) {

            if (other.getId().equals(claim.getId())) {
                continue;
            }

            boolean sameCustomer =
                    other.getCustomer().getId().equals(customerId);

            boolean samePolicy =
                    other.getPolicyVehicle().getPolicy().getId()
                            .equals(policyId);

            boolean sameAmount =
                    other.getClaimedAmount()
                            .compareTo(claim.getClaimedAmount()) == 0;

            if (sameCustomer && samePolicy && sameAmount) {

                hits.add(new RuleHit(
                        "R01",
                        "Possible exact duplicate claim: "
                                + other.getClaimNumber(),
                        other,
                        EntityMatchType.DUPLICATE_CLAIM,
                        new BigDecimal("0.9900")
                ));

                exactDuplicateFound = true;
                break;
            }
        }

        if (!exactDuplicateFound) {

            sameVehicleAndDate.stream()
                    .filter(other ->
                            !other.getId().equals(claim.getId())
                    )
                    .findFirst()
                    .ifPresent(other ->
                            hits.add(new RuleHit(
                                    "R02",
                                    "Another claim exists for this vehicle and incident date",
                                    other,
                                    EntityMatchType.SAME_VEHICLE,
                                    new BigDecimal("0.9000")
                            ))
                    );
        }

        LocalDate startDate = incidentDate.minusDays(90);

        long previousClaims =
                claimRepository.countByCustomer_IdAndIncidentDateBetween(
                        customerId,
                        startDate,
                        incidentDate.minusDays(1)
                );

        if (previousClaims >= 2) {

            hits.add(new RuleHit(
                    "R03",
                    "Customer has " + previousClaims
                            + " previous claims in the preceding 90 days",
                    null,
                    null,
                    null
            ));
        }

        List<EntityMatch> existingMatches =
                matchRepository.findByClaim_Id(claim.getId());

        boolean repeatedInvoice = existingMatches.stream()
                .anyMatch(match ->
                        match.getMatchType()
                                == EntityMatchType.REPEATED_INVOICE
                );

        boolean sharedRepairer = existingMatches.stream()
                .anyMatch(match ->
                        match.getMatchType()
                                == EntityMatchType.SAME_REPAIRER
                );

        if (repeatedInvoice) {

            hits.add(new RuleHit(
                    "R04",
                    "A repair invoice identifier matches another claim",
                    null,
                    null,
                    null
            ));
        }

        if (sharedRepairer) {

            hits.add(new RuleHit(
                    "R05",
                    "Repairer identifiers are shared across multiple customers",
                    null,
                    null,
                    null
            ));
        }

        Vehicle vehicle = claim.getPolicyVehicle().getVehicle();

        String reportedRegistration =
                normalize(claim.getReportedRegistrationNumber());

        String actualRegistration =
                normalize(vehicle.getRegistrationNumber());

        String reportedVin =
                normalize(claim.getReportedVin());

        String actualVin =
                normalize(vehicle.getVin());

        boolean registrationMismatch =
                reportedRegistration != null
                        && !reportedRegistration.equals(actualRegistration);

        boolean vinMismatch =
                reportedVin != null
                        && !reportedVin.equals(actualVin);

        if (registrationMismatch || vinMismatch) {

            String reason;

            if (registrationMismatch && vinMismatch) {

                reason = "Reported registration number and VIN do not match the registered vehicle";

            } else if (registrationMismatch) {

                reason = "Reported registration number does not match the registered vehicle";

            } else {

                reason = "Reported VIN does not match the registered vehicle";
            }

            hits.add(new RuleHit(
                    "R06",
                    reason,
                    null,
                    null,
                    null
            ));
        }

        flagRepository.deleteByClaim_Id(claim.getId());

        int totalScore = 0;
        int enabledRuleCount = 0;

        for (RuleHit hit : hits) {

            FraudRuleConfiguration rule =
                    ruleRepository.findByRuleCode(hit.ruleCode())
                            .filter(r ->
                                    Boolean.TRUE.equals(r.getEnabled())
                            )
                            .orElse(null);

            if (rule == null) {
                continue;
            }

            int contribution = Math.max(0, rule.getWeight());

            totalScore += contribution;
            enabledRuleCount++;

            ClaimFlag flag = new ClaimFlag();

            flag.setClaim(claim);
            flag.setRuleConfiguration(rule);
            flag.setRuleCodeSnapshot(rule.getRuleCode());
            flag.setScoreContribution(contribution);
            flag.setReason(hit.reason());

            flagRepository.save(flag);

            if (hit.matchedClaim() != null
                    && hit.matchType() != null) {

                createClaimMatch(claim, hit);
            }
        }

        int cappedScore = Math.min(totalScore, 100);

        RiskBand band = cappedScore >= 50
                ? RiskBand.HIGH
                : cappedScore >= 20
                        ? RiskBand.MEDIUM
                        : RiskBand.LOW;

        claim.setRiskScore(cappedScore);
        claim.setRiskBand(band);

        if (band == RiskBand.LOW) {

            claim.setStatus(ClaimStatus.CLEARED_FOR_PROCESSING);

        } else {

            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        }

        return new EvaluationResult(
                cappedScore,
                band,
                enabledRuleCount
        );
    }

    private void createClaimMatch(
            Claim claim,
            RuleHit hit
    ) {

        boolean exists = matchRepository
                .findByClaim_Id(claim.getId())
                .stream()
                .anyMatch(match ->
                        match.getMatchedClaim().getId()
                                .equals(hit.matchedClaim().getId())
                                && match.getMatchType() == hit.matchType()
                );

        if (exists) {
            return;
        }

        EntityMatch match = new EntityMatch();

        match.setClaim(claim);
        match.setMatchedClaim(hit.matchedClaim());
        match.setMatchType(hit.matchType());
        match.setConfidenceScore(hit.confidence());

        match.setMatchDetails(
                objectMapper.valueToTree(
                        java.util.Map.of(
                                "ruleCode",
                                hit.ruleCode()
                        )
                )
        );

        match.setReviewStatus(EntityMatchReviewStatus.PENDING);

        matchRepository.save(match);
    }

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }

    public record EvaluationResult(
            int riskScore,
            RiskBand riskBand,
            int matchedRuleCount
    ) {
    }

    private record RuleHit(
            String ruleCode,
            String reason,
            Claim matchedClaim,
            EntityMatchType matchType,
            BigDecimal confidence
    ) {
    }
}
