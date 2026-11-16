package com.insurance.repository;
import com.insurance.entity.EvidenceRequest;
import com.insurance.enums.EvidenceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EvidenceRequestRepository extends JpaRepository<EvidenceRequest, Long> {
    List<EvidenceRequest> findByInvestigation_IdOrderByRequestedAtDesc(Long investigationId);
    long countByInvestigation_IdAndStatus(Long investigationId, EvidenceRequestStatus status);
    List<EvidenceRequest> findByStatusOrderByRequestedAtAsc(EvidenceRequestStatus status);
    List<EvidenceRequest> findByInvestigation_Claim_SubmittedBy_IdAndStatusOrderByRequestedAtDesc(Long userId,
            EvidenceRequestStatus status);
    Optional<EvidenceRequest> findByIdAndInvestigation_Claim_SubmittedBy_Id(Long requestId, Long userId);
}
