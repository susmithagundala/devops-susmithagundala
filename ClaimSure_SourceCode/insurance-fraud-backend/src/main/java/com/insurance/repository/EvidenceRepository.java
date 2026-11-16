package com.insurance.repository;
import com.insurance.entity.Evidence;
import com.insurance.enums.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByClaim_IdOrderByUploadedAtDesc(Long claimId);
    List<Evidence> findByClaim_IdAndEvidenceType(Long claimId, EvidenceType evidenceType);
    Optional<Evidence> findByIdAndClaim_Id(Long evidenceId, Long claimId);
}
