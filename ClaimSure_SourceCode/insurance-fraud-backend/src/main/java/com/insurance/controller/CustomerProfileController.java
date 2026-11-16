package com.insurance.controller;
import com.insurance.dto.CustomerProfileResponse;
import com.insurance.dto.UpdateCustomerProfileRequest;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.CustomerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CLAIMANT')")
public class CustomerProfileController {
    private final CustomerProfileService profileService;
    public CustomerProfileController(CustomerProfileService profileService) {
        this.profileService = profileService;
    }
    @GetMapping
    public CustomerProfileResponse getProfile(@AuthenticationPrincipal CustomUserDetails user) {
        return profileService.getProfile(user.getId());
    }
    @PutMapping
    public CustomerProfileResponse updateProfile(@Valid @RequestBody UpdateCustomerProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        return profileService.updateProfile(user.getId(), request);
    }
}
