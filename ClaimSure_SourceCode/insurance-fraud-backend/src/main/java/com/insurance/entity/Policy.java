package com.insurance.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Entity
@Table(name = "policies")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    private String policyNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "coverage_type", nullable = false, length = 50)
    private String coverageType;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @Column(name = "premium_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal premiumAmount;
    @Column(name = "coverage_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal coverageLimit;
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    protected Policy() {
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
    public String getPolicyNumber() {
        return policyNumber;
    }
    public void setPolicyNumber(String v) {
        policyNumber = v;
    }
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer v) {
        customer = v;
    }
    public String getCoverageType() {
        return coverageType;
    }
    public void setCoverageType(String v) {
        coverageType = v;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate v) {
        startDate = v;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate v) {
        endDate = v;
    }
    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }
    public void setPremiumAmount(BigDecimal v) {
        premiumAmount = v;
    }
    public BigDecimal getCoverageLimit() {
        return coverageLimit;
    }
    public void setCoverageLimit(BigDecimal v) {
        coverageLimit = v;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String v) {
        status = v;
    }
}
