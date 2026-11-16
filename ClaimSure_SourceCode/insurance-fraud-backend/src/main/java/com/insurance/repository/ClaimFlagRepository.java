package com.insurance.repository;
import com.insurance.entity.ClaimFlag;
import com.insurance.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ClaimFlagRepository extends JpaRepository<ClaimFlag, Long> {
    List<ClaimFlag> findByClaim_IdOrderByCreatedAtAsc(Long claimId);
    List<ClaimFlag> findByClaim_StatusOrderByClaim_RiskScoreDesc(ClaimStatus status);
    boolean existsByClaim_IdAndRuleConfiguration_Id(Long claimId, Long ruleId);
    void deleteByClaim_Id(Long claimId);
}
