package com.insurance.service;
import com.insurance.dto.PolicyResponse;
import com.insurance.dto.VehicleResponse;
import com.insurance.entity.PolicyVehicle;
import com.insurance.repository.CustomerRepository;
import com.insurance.repository.PolicyRepository;
import com.insurance.repository.PolicyVehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
@Service
public class PolicyService {
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final PolicyVehicleRepository policyVehicleRepository;
    public PolicyService(CustomerRepository customerRepository, PolicyRepository policyRepository,
            PolicyVehicleRepository policyVehicleRepository) {
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.policyVehicleRepository = policyVehicleRepository;
    }
    @Transactional(readOnly = true)
    public List<PolicyResponse> getMyPolicies(Long userId) {
        var customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found"));
        return policyRepository.findByCustomer_Id(customer.getId()).stream()
                .map(p -> new PolicyResponse(p.getId(), p.getPolicyNumber(), p.getCoverageType(), p.getStartDate(),
                        p.getEndDate(), p.getCoverageLimit(), p.getStatus()))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesForPolicy(Long policyId, Long userId) {
        var customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found"));
        var policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access this policy");
        }
        if (!"ACTIVE".equalsIgnoreCase(policy.getStatus()) || LocalDate.now().isBefore(policy.getStartDate())
                || LocalDate.now().isAfter(policy.getEndDate())) {
            return List.of();
        }
        return policyVehicleRepository.findByPolicy_Id(policyId).stream().filter(pv -> {
            LocalDate today = LocalDate.now();
            return !today.isBefore(pv.getCoverageStartDate()) && !today.isAfter(pv.getCoverageEndDate());
        }).map(PolicyVehicle::getVehicle).map(v -> new VehicleResponse(v.getId(), v.getRegistrationNumber(), v.getVin(),
                v.getMake(), v.getModel(), v.getManufacturingYear())).toList();
    }
}