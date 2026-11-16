package com.insurance.dto;
public record VehicleResponse(Long id, String registrationNumber, String vin, String make, String model,
        Integer manufacturingYear) {
}