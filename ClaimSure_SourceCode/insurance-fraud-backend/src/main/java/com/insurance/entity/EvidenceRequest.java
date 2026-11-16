package com.insurance.entity;
import com.insurance.enums.EvidenceRequestStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "evidence_requests")
public class EvidenceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private AppUser requestedBy;
    @Column(nullable = false, columnDefinition = "text")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EvidenceRequestStatus status = EvidenceRequestStatus.PENDING;
    @Column(name = "requested_at", nullable = false, updatable = false)
    private OffsetDateTime requestedAt;
    @Column(name = "due_at")
    private OffsetDateTime dueAt;
    @Column(name = "fulfilled_at")
    private OffsetDateTime fulfilledAt;
    public EvidenceRequest() {
    }
    @PrePersist
    protected void onCreate() {
        requestedAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public Investigation getInvestigation() {
        return investigation;
    }
    public void setInvestigation(Investigation v) {
        investigation = v;
    }
    public AppUser getRequestedBy() {
        return requestedBy;
    }
    public void setRequestedBy(AppUser v) {
        requestedBy = v;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String v) {
        description = v;
    }
    public EvidenceRequestStatus getStatus() {
        return status;
    }
    public void setStatus(EvidenceRequestStatus v) {
        status = v;
    }
    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }
    public OffsetDateTime getDueAt() {
        return dueAt;
    }
    public void setDueAt(OffsetDateTime v) {
        dueAt = v;
    }
    public OffsetDateTime getFulfilledAt() {
        return fulfilledAt;
    }
    public void setFulfilledAt(OffsetDateTime v) {
        fulfilledAt = v;
    }
}
