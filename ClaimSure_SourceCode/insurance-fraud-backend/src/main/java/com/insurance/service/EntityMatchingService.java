package com.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.entity.Claim;
import com.insurance.entity.EntityMatch;
import com.insurance.entity.RepairInvoice;
import com.insurance.enums.EntityMatchReviewStatus;
import com.insurance.enums.EntityMatchType;
import com.insurance.repository.EntityMatchRepository;
import com.insurance.repository.RepairInvoiceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EntityMatchingService {

    private static final int R05_MIN_DISTINCT_CUSTOMERS = 3;

    private final EntityMatchRepository matchRepository;
    private final RepairInvoiceRepository invoiceRepository;
    private final ObjectMapper objectMapper;

    public EntityMatchingService(
            EntityMatchRepository matchRepository,
            RepairInvoiceRepository invoiceRepository,
            ObjectMapper objectMapper
    ) {
        this.matchRepository = matchRepository;
        this.invoiceRepository = invoiceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int evaluateInvoiceMatches(
            Claim claim,
            RepairInvoice invoice
    ) {
        int created = 0;

        List<RepairInvoice> sameNumber =
                invoiceRepository.findByInvoiceNumberIgnoreCase(
                        invoice.getInvoiceNumber()
                );

        for (RepairInvoice existing : sameNumber) {
            Claim otherClaim = existing.getEvidence().getClaim();

            if (otherClaim.getId().equals(claim.getId())) {
                continue;
            }

            if (createMatchIfMissing(
                    claim,
                    otherClaim,
                    EntityMatchType.REPEATED_INVOICE,
                    new BigDecimal("0.9900"),
                    Map.of(
                            "invoiceNumber",
                            invoice.getInvoiceNumber()
                    )
            )) {
                created++;
            }
        }

        Map<Long, Claim> matchedClaims = new HashMap<>();

        if (invoice.getRepairerGstin() != null
                && !invoice.getRepairerGstin().isBlank()) {

            List<RepairInvoice> sameGstin =
                    invoiceRepository.findByRepairerGstinIgnoreCase(
                            invoice.getRepairerGstin()
                    );

            collectOtherClaims(claim, sameGstin, matchedClaims);
        }

        if (invoice.getRepairerPhone() != null
                && !invoice.getRepairerPhone().isBlank()) {

            List<RepairInvoice> samePhone =
                    invoiceRepository.findByRepairerPhone(
                            invoice.getRepairerPhone()
                    );

            collectOtherClaims(claim, samePhone, matchedClaims);
        }

        Set<Long> distinctCustomerIds = new HashSet<>();

        distinctCustomerIds.add(claim.getCustomer().getId());

        for (Claim matched : matchedClaims.values()) {
            distinctCustomerIds.add(matched.getCustomer().getId());
        }

        if (distinctCustomerIds.size() >= R05_MIN_DISTINCT_CUSTOMERS) {

            Map<String, Object> details = new HashMap<>();

            details.put("repairerGstin", invoice.getRepairerGstin());
            details.put("repairerPhone", invoice.getRepairerPhone());
            details.put(
                    "distinctCustomerCount",
                    distinctCustomerIds.size()
            );

            for (Claim otherClaim : matchedClaims.values()) {

                if (otherClaim.getCustomer().getId()
                        .equals(claim.getCustomer().getId())) {
                    continue;
                }

                if (createMatchIfMissing(
                        claim,
                        otherClaim,
                        EntityMatchType.SAME_REPAIRER,
                        new BigDecimal("0.8000"),
                        details
                )) {
                    created++;
                }
            }
        }

        return created;
    }

    private void collectOtherClaims(
            Claim currentClaim,
            List<RepairInvoice> invoices,
            Map<Long, Claim> matchedClaims
    ) {
        for (RepairInvoice existing : invoices) {

            Claim otherClaim = existing.getEvidence().getClaim();

            if (otherClaim.getId().equals(currentClaim.getId())) {
                continue;
            }

            matchedClaims.put(otherClaim.getId(), otherClaim);
        }
    }

    private boolean createMatchIfMissing(
            Claim claim,
            Claim otherClaim,
            EntityMatchType type,
            BigDecimal confidence,
            Map<String, Object> details
    ) {

        boolean existsInCurrentDirection =
                matchRepository.findByClaim_Id(claim.getId())
                        .stream()
                        .anyMatch(match ->
                                match.getMatchedClaim().getId()
                                        .equals(otherClaim.getId())
                                        && match.getMatchType() == type
                        );

        boolean existsInReverseDirection =
                matchRepository.findByMatchedClaim_Id(claim.getId())
                        .stream()
                        .anyMatch(match ->
                                match.getClaim().getId()
                                        .equals(otherClaim.getId())
                                        && match.getMatchType() == type
                        );

        if (existsInCurrentDirection || existsInReverseDirection) {
            return false;
        }

        EntityMatch match = new EntityMatch();

        match.setClaim(claim);
        match.setMatchedClaim(otherClaim);
        match.setMatchType(type);
        match.setConfidenceScore(confidence);
        match.setReviewStatus(EntityMatchReviewStatus.PENDING);

        match.setMatchDetails(objectMapper.valueToTree(details));

        matchRepository.save(match);

        return true;
    }
}
