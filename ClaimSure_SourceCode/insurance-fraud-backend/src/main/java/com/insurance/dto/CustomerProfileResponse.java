package com.insurance.dto;
import java.time.LocalDate;
public record CustomerProfileResponse(Long customerId, String email, String firstName, String lastName,
        LocalDate dateOfBirth, String phone, String addressLine1, String addressLine2, String city, String state,
        String postalCode, String country) {
}
