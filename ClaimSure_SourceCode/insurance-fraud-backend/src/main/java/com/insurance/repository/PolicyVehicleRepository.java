package com.insurance.repository;
import com.insurance.entity.PolicyVehicle;
import com.insurance.entity.embedded.PolicyVehicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PolicyVehicleRepository extends JpaRepository<PolicyVehicle, PolicyVehicleId> {
    List<PolicyVehicle> findByPolicy_Id(Long policyId);
    List<PolicyVehicle> findByVehicle_Id(Long vehicleId);
    Optional<PolicyVehicle> findByPolicy_IdAndVehicle_Id(Long policyId, Long vehicleId);
    boolean existsByPolicy_IdAndVehicle_Id(Long policyId, Long vehicleId);
}
