package com.insurance.repository;
import com.insurance.entity.EntityMatch;
import com.insurance.enums.EntityMatchReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EntityMatchRepository extends JpaRepository<EntityMatch, Long> {
    List<EntityMatch> findByClaim_Id(Long claimId);
    List<EntityMatch> findByMatchedClaim_Id(Long matchedClaimId);
    List<EntityMatch> findByReviewStatusOrderByCreatedAtDesc(EntityMatchReviewStatus status);
}
