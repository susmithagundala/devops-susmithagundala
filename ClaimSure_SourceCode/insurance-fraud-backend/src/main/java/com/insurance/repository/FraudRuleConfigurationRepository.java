package com.insurance.repository;
import com.insurance.entity.FraudRuleConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FraudRuleConfigurationRepository extends JpaRepository<FraudRuleConfiguration, Long> {
    Optional<FraudRuleConfiguration> findByRuleCode(String ruleCode);
    List<FraudRuleConfiguration> findByEnabledTrueOrderByRuleCodeAsc();
}
