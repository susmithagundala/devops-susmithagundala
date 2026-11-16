package com.insurance.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "claim_flags", uniqueConstraints = @UniqueConstraint(name = "uq_claim_rule_flag", columnNames = {
        "claim_id", "rule_configuration_id" }))
public class ClaimFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_configuration_id", nullable = false)
    private FraudRuleConfiguration ruleConfiguration;
    @Column(name = "rule_code_snapshot", nullable = false, length = 20)
    private String ruleCodeSnapshot;
    @Column(name = "score_contribution", nullable = false)
    private Integer scoreContribution;
    @Column(nullable = false)
    private String reason;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    public ClaimFlag() {
    }
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
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
    public FraudRuleConfiguration getRuleConfiguration() {
        return ruleConfiguration;
    }
    public void setRuleConfiguration(FraudRuleConfiguration v) {
        ruleConfiguration = v;
    }
    public String getRuleCodeSnapshot() {
        return ruleCodeSnapshot;
    }
    public void setRuleCodeSnapshot(String v) {
        ruleCodeSnapshot = v;
    }
    public Integer getScoreContribution() {
        return scoreContribution;
    }
    public void setScoreContribution(Integer v) {
        scoreContribution = v;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String v) {
        reason = v;
    }
}
