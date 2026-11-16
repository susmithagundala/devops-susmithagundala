package com.insurance.entity;
import com.insurance.enums.ClaimStatus;
import com.insurance.enums.RiskBand;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({ @JoinColumn(name = "policy_id", referencedColumnName = "policy_id", nullable = false),
            @JoinColumn(name = "vehicle_id", referencedColumnName = "vehicle_id", nullable = false) })
    private PolicyVehicle policyVehicle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private AppUser submittedBy;
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;
    @Column(name = "incident_location")
    private String incidentLocation;
    @Column(name = "incident_description", nullable = false)
    private String incidentDescription;
    @Column(name = "reported_registration_number", length = 20)
    private String reportedRegistrationNumber;
    @Column(name = "reported_vin", length = 50)
    private String reportedVin;
    @Column(name = "claimed_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal claimedAmount;
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore = 0;
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_band", nullable = false, length = 20)
    private RiskBand riskBand = RiskBand.LOW;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ClaimStatus status = ClaimStatus.SUBMITTED;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    public Claim() {
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
    public String getClaimNumber() {
        return claimNumber;
    }
    public void setClaimNumber(String v) {
        claimNumber = v;
    }
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer v) {
        customer = v;
    }
    public PolicyVehicle getPolicyVehicle() {
        return policyVehicle;
    }
    public void setPolicyVehicle(PolicyVehicle v) {
        policyVehicle = v;
    }
    public AppUser getSubmittedBy() {
        return submittedBy;
    }
    public void setSubmittedBy(AppUser v) {
        submittedBy = v;
    }
    public LocalDate getIncidentDate() {
        return incidentDate;
    }
    public void setIncidentDate(LocalDate v) {
        incidentDate = v;
    }
    public String getIncidentLocation() {
        return incidentLocation;
    }
    public void setIncidentLocation(String v) {
        incidentLocation = v;
    }
    public String getIncidentDescription() {
        return incidentDescription;
    }
    public void setIncidentDescription(String v) {
        incidentDescription = v;
    }
    public BigDecimal getClaimedAmount() {
        return claimedAmount;
    }
    public void setClaimedAmount(BigDecimal v) {
        claimedAmount = v;
    }
    public Integer getRiskScore() {
        return riskScore;
    }
    public void setRiskScore(Integer v) {
        riskScore = v;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public String getReportedRegistrationNumber() {
        return reportedRegistrationNumber;
    }
    public void setReportedRegistrationNumber(String value) {
        this.reportedRegistrationNumber = value;
    }
    public String getReportedVin() {
        return reportedVin;
    }
    public void setReportedVin(String value) {
        this.reportedVin = value;
    }
    public RiskBand getRiskBand() {
        return riskBand;
    }
    public void setRiskBand(RiskBand v) {
        riskBand = v;
    }
    public ClaimStatus getStatus() {
        return status;
    }
    public void setStatus(ClaimStatus v) {
        status = v;
    }
}
