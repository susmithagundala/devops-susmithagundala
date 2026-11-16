package com.insurance.dto;
import com.insurance.enums.EvidenceRequestStatus;
import java.time.OffsetDateTime;
public record EvidenceRequestResponse(Long id, Long claimId, String claimNumber, String description,
        EvidenceRequestStatus status, OffsetDateTime requestedAt, OffsetDateTime dueAt, OffsetDateTime fulfilledAt) {
}
