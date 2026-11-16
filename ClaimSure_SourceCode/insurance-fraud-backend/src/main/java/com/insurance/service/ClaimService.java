package com.insurance.service;
import com.insurance.dto.ClaimSubmissionRequest;
import com.insurance.dto.ClaimSummaryResponse;
import com.insurance.entity.*;
import com.insurance.repository.*;
import com.insurance.util.ClaimNumberGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
@Service
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final CustomerRepository customerRepository;
    private final AppUserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final PolicyVehicleRepository policyVehicleRepository;
    private final FraudDetectionService fraudDetectionService;
    private final ClaimNumberGenerator claimNumberGenerator;
    public ClaimService(ClaimRepository claimRepository, CustomerRepository customerRepository,
            AppUserRepository userRepository, PolicyRepository policyRepository, VehicleRepository vehicleRepository,
            PolicyVehicleRepository policyVehicleRepository, FraudDetectionService fraudDetectionService,
            ClaimNumberGenerator claimNumberGenerator) {
        this.claimRepository = claimRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.policyRepository = policyRepository;
        this.vehicleRepository = vehicleRepository;
        this.policyVehicleRepository = policyVehicleRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.claimNumberGenerator = claimNumberGenerator;
    }
    @Transactional
    public ClaimSummaryResponse submit(ClaimSubmissionRequest request, Long userId) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claimant profile was not found"));
        AppUser claimant = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
        if (request.incidentDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incident date cannot be in the future");
        }
        Policy policy = policyRepository.findById(request.policyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy was not found"));
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle was not found"));
        if (!policy.getCustomer().getId().equals(customer.getId())
                || !vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The selected policy and vehicle must belong to the authenticated claimant");
        }
        if (!"ACTIVE".equalsIgnoreCase(policy.getStatus()) || request.incidentDate().isBefore(policy.getStartDate())
                || request.incidentDate().isAfter(policy.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy was not active on the incident date");
        }
        PolicyVehicle policyVehicle = policyVehicleRepository
                .findByPolicy_IdAndVehicle_Id(request.policyId(), request.vehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This vehicle is not associated with the selected policy"));
        if (request.incidentDate().isBefore(policyVehicle.getCoverageStartDate())
                || request.incidentDate().isAfter(policyVehicle.getCoverageEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vehicle was not covered by this policy on the incident date");
        }
        Claim claim = new Claim();
        claim.setClaimNumber(claimNumberGenerator.generate());
        claim.setCustomer(customer);
        claim.setPolicyVehicle(policyVehicle);
        claim.setSubmittedBy(claimant);
        claim.setIncidentDate(request.incidentDate());
        claim.setIncidentLocation(request.incidentLocation().trim());
        claim.setIncidentDescription(request.incidentDescription().trim());
        claim.setClaimedAmount(request.claimedAmount());
        claim.setReportedRegistrationNumber(request.reportedRegistrationNumber());
        claim.setReportedVin(request.reportedVin());
        claim = claimRepository.saveAndFlush(claim);
        fraudDetectionService.evaluate(claim);
        return toResponse(claimRepository.save(claim));
    }
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getMyClaims(Long userId) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claimant profile was not found"));
        return claimRepository.findByCustomer_IdOrderByIdDesc(customer.getId()).stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public ClaimSummaryResponse getMyClaim(Long claimId, Long userId) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claimant profile was not found"));
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim was not found"));
        if (!claim.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim was not found");
        }
        return toResponse(claim);
    }
    private ClaimSummaryResponse toResponse(Claim claim) {
        return new ClaimSummaryResponse(claim.getId(), claim.getClaimNumber(),
                claim.getPolicyVehicle().getPolicy().getId(), claim.getPolicyVehicle().getVehicle().getId(),
                claim.getIncidentDate(), claim.getIncidentLocation(), claim.getIncidentDescription(),
                claim.getClaimedAmount(), claim.getRiskScore(), claim.getRiskBand(), claim.getStatus(),
                claim.getCreatedAt());
    }
}
