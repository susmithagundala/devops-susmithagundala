package com.insurance.controller;
import com.insurance.dto.ClaimSubmissionRequest;
import com.insurance.dto.ClaimSummaryResponse;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/claims")
@PreAuthorize("hasRole('CLAIMANT')")
public class ClaimController {
    private final ClaimService claimService;
    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimSummaryResponse submit(@Valid @RequestBody ClaimSubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        return claimService.submit(request, user.getId());
    }
    @GetMapping
    public List<ClaimSummaryResponse> getMyClaims(@AuthenticationPrincipal CustomUserDetails user) {
        return claimService.getMyClaims(user.getId());
    }
    @GetMapping("/{claimId}")
    public ClaimSummaryResponse getMyClaim(@PathVariable Long claimId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return claimService.getMyClaim(claimId, user.getId());
    }
}
