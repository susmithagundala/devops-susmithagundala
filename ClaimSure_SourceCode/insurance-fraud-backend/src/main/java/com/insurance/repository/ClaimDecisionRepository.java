package com.insurance.repository;
import com.insurance.entity.ClaimDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ClaimDecisionRepository extends JpaRepository<ClaimDecision, Long> {
    Optional<ClaimDecision> findByClaim_Id(Long claimId);
    boolean existsByClaim_Id(Long claimId);
}