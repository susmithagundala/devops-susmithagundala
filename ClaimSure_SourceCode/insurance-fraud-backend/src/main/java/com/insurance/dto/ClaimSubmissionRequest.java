package com.insurance.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ClaimSubmissionRequest(@NotNull Long policyId, @NotNull Long vehicleId, @NotNull LocalDate incidentDate,
        @NotBlank @Size(max = 255) String incidentLocation,
        @NotBlank @Size(max = 4000) String incidentDescription,
        @NotNull @DecimalMin(value = "0.01") BigDecimal claimedAmount,
        @Size(max = 20) String reportedRegistrationNumber,
        @Size(max = 50) String reportedVin) {
}