package com.insurance.service;
import com.insurance.dto.CustomerProfileResponse;
import com.insurance.dto.UpdateCustomerProfileRequest;
import com.insurance.entity.Customer;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class CustomerProfileService {
    private final CustomerRepository customerRepository;
    public CustomerProfileService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long userId) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        return mapToResponse(customer);
    }
    @Transactional
    public CustomerProfileResponse updateProfile(Long userId, UpdateCustomerProfileRequest request) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setPhone(clean(request.phone()));
        customer.setAddressLine1(clean(request.addressLine1()));
        customer.setAddressLine2(clean(request.addressLine2()));
        customer.setCity(clean(request.city()));
        customer.setState(clean(request.state()));
        customer.setPostalCode(clean(request.postalCode()));
        customer.setCountry(clean(request.country()));
        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }
    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private CustomerProfileResponse mapToResponse(Customer customer) {
        return new CustomerProfileResponse(customer.getId(), customer.getUser().getEmail(), customer.getFirstName(),
                customer.getLastName(), customer.getDateOfBirth(), customer.getPhone(), customer.getAddressLine1(),
                customer.getAddressLine2(), customer.getCity(), customer.getState(), customer.getPostalCode(),
                customer.getCountry());
    }
}
