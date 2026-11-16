package com.insurance.entity;
import com.insurance.enums.DecisionType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "claim_decisions")
public class ClaimDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decided_by_user_id", nullable = false)
    private AppUser decidedBy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DecisionType decision;
    @Column(columnDefinition = "text")
    private String reason;
    @Column(name = "decided_at", nullable = false, updatable = false)
    private OffsetDateTime decidedAt;
    public ClaimDecision() {
    }
    @PrePersist
    protected void onCreate() {
        decidedAt = OffsetDateTime.now();
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
    public Investigation getInvestigation() {
        return investigation;
    }
    public void setInvestigation(Investigation v) {
        investigation = v;
    }
    public AppUser getDecidedBy() {
        return decidedBy;
    }
    public void setDecidedBy(AppUser v) {
        decidedBy = v;
    }
    public DecisionType getDecision() {
        return decision;
    }
    public void setDecision(DecisionType v) {
        decision = v;
    }
    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String v) {
        reason = v;
    }
}
