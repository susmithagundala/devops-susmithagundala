package com.insurance.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record PolicyResponse(Long id, String policyNumber, String coverageType, LocalDate startDate, LocalDate endDate,
        BigDecimal coverageLimit, String status) {
}