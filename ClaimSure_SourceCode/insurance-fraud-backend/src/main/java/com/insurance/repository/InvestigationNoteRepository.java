package com.insurance.repository;
import com.insurance.entity.InvestigationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InvestigationNoteRepository extends JpaRepository<InvestigationNote, Long> {
    List<InvestigationNote> findByInvestigation_IdOrderByCreatedAtAsc(Long investigationId);
}
