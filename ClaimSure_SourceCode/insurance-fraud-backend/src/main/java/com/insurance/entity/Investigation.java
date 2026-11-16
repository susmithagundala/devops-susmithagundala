package com.insurance.entity;
import com.insurance.enums.InvestigationStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "investigations")
public class Investigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigator_user_id", nullable = false)
    private AppUser investigator;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestigationStatus status = InvestigationStatus.OPEN;
    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    public Investigation() {
    }
    @PrePersist
    protected void onCreate() {
        OffsetDateTime n = OffsetDateTime.now();
        startedAt = startedAt == null ? n : startedAt;
        createdAt = n;
        updatedAt = n;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public Claim getClaim() {
        return claim;
    }
    public void setClaim(Claim v) {
        claim = v;
    }
    public AppUser getInvestigator() {
        return investigator;
    }
    public void setInvestigator(AppUser v) {
        investigator = v;
    }
    public InvestigationStatus getStatus() {
        return status;
    }
    public void setStatus(InvestigationStatus v) {
        status = v;
    }
    public OffsetDateTime getStartedAt() {
        return startedAt;
    }
    public void setStartedAt(OffsetDateTime v) {
        startedAt = v;
    }
    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(OffsetDateTime v) {
        completedAt = v;
    }
}
