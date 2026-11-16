package com.insurance.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Entity
@Table(name = "repair_invoices")
public class RepairInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", nullable = false, unique = true)
    private Evidence evidence;
    @Column(name = "invoice_number", nullable = false, length = 100)
    private String invoiceNumber;
    @Column(name = "repairer_name", nullable = false)
    private String repairerName;
    @Column(name = "repairer_gstin", length = 15)
    private String repairerGstin;
    @Column(name = "repairer_phone", length = 20)
    private String repairerPhone;
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    @Column(name = "invoice_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal invoiceAmount;
    @Column(nullable = false, length = 3)
    private String currency = "INR";
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    public RepairInvoice() {
    }
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public Evidence getEvidence() {
        return evidence;
    }
    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    public String getRepairerName() {
        return repairerName;
    }
    public void setRepairerName(String repairerName) {
        this.repairerName = repairerName;
    }
    public String getRepairerGstin() {
        return repairerGstin;
    }
    public void setRepairerGstin(String repairerGstin) {
        this.repairerGstin = repairerGstin;
    }
    public String getRepairerPhone() {
        return repairerPhone;
    }
    public void setRepairerPhone(String repairerPhone) {
        this.repairerPhone = repairerPhone;
    }
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }
    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    public BigDecimal getInvoiceAmount() {
        return invoiceAmount;
    }
    public void setInvoiceAmount(BigDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
