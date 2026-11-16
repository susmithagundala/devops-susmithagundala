package com.insurance.entity.embedded;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class PolicyVehicleId implements Serializable {
    @Column(name = "policy_id")
    private Long policyId;
    @Column(name = "vehicle_id")
    private Long vehicleId;
    protected PolicyVehicleId() {
    }
    public PolicyVehicleId(Long policyId, Long vehicleId) {
        this.policyId = policyId;
        this.vehicleId = vehicleId;
    }
    public Long getPolicyId() {
        return policyId;
    }
    public Long getVehicleId() {
        return vehicleId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PolicyVehicleId that))
            return false;
        return Objects.equals(policyId, that.policyId) && Objects.equals(vehicleId, that.vehicleId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(policyId, vehicleId);
    }
}
