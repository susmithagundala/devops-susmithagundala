package com.insurance.repository;
import com.insurance.entity.Investigation;
import com.insurance.enums.InvestigationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    Optional<Investigation> findByClaim_Id(Long claimId);
    List<Investigation> findByInvestigator_IdAndStatusOrderByStartedAtAsc(Long investigatorId,
            InvestigationStatus status);
    List<Investigation> findByInvestigator_IdOrderByStartedAtDesc(Long investigatorId);
    boolean existsByClaim_Id(Long claimId);
}
