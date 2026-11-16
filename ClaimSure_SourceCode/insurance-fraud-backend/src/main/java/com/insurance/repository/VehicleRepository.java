package com.insurance.repository;
import com.insurance.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);
    Optional<Vehicle> findByVinIgnoreCase(String vin);
    List<Vehicle> findByCustomer_Id(Long customerId);
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
    boolean existsByVinIgnoreCase(String vin);
}
