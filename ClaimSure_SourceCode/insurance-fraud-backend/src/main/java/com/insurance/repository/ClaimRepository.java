package com.insurance.repository;
import com.insurance.entity.Claim;
import com.insurance.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumberIgnoreCase(String claimNumber);
    List<Claim> findByCustomer_IdOrderByIdDesc(Long customerId);
    List<Claim> findByStatusOrderByCreatedAtAsc(ClaimStatus status);
    List<Claim> findByRiskBandOrderByRiskScoreDesc(com.insurance.enums.RiskBand riskBand);
    boolean existsByClaimNumberIgnoreCase(String claimNumber);
    List<Claim> findByPolicyVehicle_Vehicle_IdAndIncidentDate(Long vehicleId, LocalDate incidentDate);
    long countByCustomer_IdAndIncidentDateBetween(Long customerId, LocalDate startDate, LocalDate endDate);
    List<Claim> findByPolicyVehicle_Policy_IdAndPolicyVehicle_Vehicle_IdAndIncidentDate(Long policyId, Long vehicleId,
            LocalDate incidentDate);
    List<Claim> findByIdIn(List<Long> ids);
}
