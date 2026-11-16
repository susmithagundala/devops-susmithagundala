package com.insurance.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String registrationNumber;
    @Column(nullable = false, unique = true, length = 50)
    private String vin;
    @Column(nullable = false, length = 100)
    private String make;
    @Column(nullable = false, length = 100)
    private String model;
    @Column(name = "manufacturing_year", nullable = false)
    private Integer manufacturingYear;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    protected Vehicle() {
    }
    @PrePersist
    protected void onCreate() {
        OffsetDateTime n = OffsetDateTime.now();
        createdAt = n;
        updatedAt = n;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer v) {
        customer = v;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public void setRegistrationNumber(String v) {
        registrationNumber = v;
    }
    public String getVin() {
        return vin;
    }
    public void setVin(String v) {
        vin = v;
    }
    public String getMake() {
        return make;
    }
    public void setMake(String v) {
        make = v;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String v) {
        model = v;
    }
    public Integer getManufacturingYear() {
        return manufacturingYear;
    }
    public void setManufacturingYear(Integer v) {
        manufacturingYear = v;
    }
}
