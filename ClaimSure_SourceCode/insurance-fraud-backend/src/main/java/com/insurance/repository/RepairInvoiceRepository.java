package com.insurance.repository;
import com.insurance.entity.RepairInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface RepairInvoiceRepository extends JpaRepository<RepairInvoice, Long> {
    Optional<RepairInvoice> findByEvidence_Id(Long evidenceId);
    List<RepairInvoice> findByInvoiceNumberIgnoreCase(String invoiceNumber);
    List<RepairInvoice> findByRepairerNameIgnoreCase(String repairerName);
    List<RepairInvoice> findByRepairerGstinIgnoreCase(String repairerGstin);
    List<RepairInvoice> findByRepairerPhone(String repairerPhone);
}
