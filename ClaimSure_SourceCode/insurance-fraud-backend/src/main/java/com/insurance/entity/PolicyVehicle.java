package com.insurance.entity;
import com.insurance.entity.embedded.PolicyVehicleId;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Entity
@Table(name = "policy_vehicles", uniqueConstraints = @UniqueConstraint(name = "uq_policy_vehicle", columnNames = {
        "policy_id", "vehicle_id" }))
public class PolicyVehicle {
    @EmbeddedId
    private PolicyVehicleId id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("policyId")
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("vehicleId")
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    @Column(name = "coverage_start_date", nullable = false)
    private LocalDate coverageStartDate;
    @Column(name = "coverage_end_date", nullable = false)
    private LocalDate coverageEndDate;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    protected PolicyVehicle() {
    }
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    public PolicyVehicleId getId() {
        return id;
    }
    public void setId(PolicyVehicleId id) {
        this.id = id;
    }
    public Policy getPolicy() {
        return policy;
    }
    public void setPolicy(Policy v) {
        policy = v;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public void setVehicle(Vehicle v) {
        vehicle = v;
    }
    public LocalDate getCoverageStartDate() {
        return coverageStartDate;
    }
    public void setCoverageStartDate(LocalDate v) {
        coverageStartDate = v;
    }
    public LocalDate getCoverageEndDate() {
        return coverageEndDate;
    }
    public void setCoverageEndDate(LocalDate v) {
        coverageEndDate = v;
    }
}
