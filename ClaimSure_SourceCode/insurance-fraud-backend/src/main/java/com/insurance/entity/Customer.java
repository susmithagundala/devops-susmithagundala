package com.insurance.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(length = 20)
    private String phone;
    @Column(name = "address_line1")
    private String addressLine1;
    @Column(name = "address_line2")
    private String addressLine2;
    private String city;
    private String state;
    @Column(name = "postal_code", length = 15)
    private String postalCode;
    private String country;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    public Customer() {
    }
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public AppUser getUser() {
        return user;
    }
    public void setUser(AppUser user) {
        this.user = user;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String v) {
        firstName = v;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String v) {
        lastName = v;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate v) {
        dateOfBirth = v;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String v) {
        phone = v;
    }
    public String getAddressLine1() {
        return addressLine1;
    }
    public void setAddressLine1(String v) {
        addressLine1 = v;
    }
    public String getAddressLine2() {
        return addressLine2;
    }
    public void setAddressLine2(String v) {
        addressLine2 = v;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city = v;
    }
    public String getState() {
        return state;
    }
    public void setState(String v) {
        state = v;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String v) {
        postalCode = v;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String v) {
        country = v;
    }
}
