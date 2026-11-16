package com.insurance.repository;
import com.insurance.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumberIgnoreCase(String policyNumber);
    List<Policy> findByCustomer_Id(Long customerId);
    boolean existsByPolicyNumberIgnoreCase(String policyNumber);
}
