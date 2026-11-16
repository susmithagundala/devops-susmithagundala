package com.insurance.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "fraud_rule_configurations")
public class FraudRuleConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rule_code", nullable = false, unique = true, length = 20)
    private String ruleCode;
    @Column(name = "rule_name", nullable = false, length = 150)
    private String ruleName;
    private String description;
    @Column(nullable = false)
    private Integer weight;
    @Column(nullable = false)
    private Boolean enabled = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    protected FraudRuleConfiguration() {
    }
    @PrePersist
    protected void onCreate() {
        OffsetDateTime n = OffsetDateTime.now();
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
    public String getRuleCode() {
        return ruleCode;
    }
    public String getRuleName() {
        return ruleName;
    }
    public String getDescription() {
        return description;
    }
    public Integer getWeight() {
        return weight;
    }
    public Boolean getEnabled() {
        return enabled;
    }
}
