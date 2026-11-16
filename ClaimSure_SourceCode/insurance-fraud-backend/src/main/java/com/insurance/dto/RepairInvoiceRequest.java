package com.insurance.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record RepairInvoiceRequest(String invoiceNumber, String repairerName, String repairerGstin,
        String repairerPhone, LocalDate invoiceDate, BigDecimal invoiceAmount) {
}
