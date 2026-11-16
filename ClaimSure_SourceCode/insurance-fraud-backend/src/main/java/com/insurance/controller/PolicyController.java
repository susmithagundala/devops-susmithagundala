package com.insurance.controller;
import com.insurance.dto.PolicyResponse;
import com.insurance.dto.VehicleResponse;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.PolicyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/policies")
@PreAuthorize("hasRole('CLAIMANT')")
public class PolicyController {
    private final PolicyService policyService;
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }
    @GetMapping
    public List<PolicyResponse> myPolicies(@AuthenticationPrincipal CustomUserDetails user) {
        return policyService.getMyPolicies(user.getId());
    }
    @GetMapping("/{policyId}/vehicles")
    public List<VehicleResponse> policyVehicles(@PathVariable Long policyId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return policyService.getVehiclesForPolicy(policyId, user.getId());
    }
}