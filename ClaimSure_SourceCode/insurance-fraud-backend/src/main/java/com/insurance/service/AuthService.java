package com.insurance.service;
import com.insurance.dto.AuthResponse;
import com.insurance.dto.LoginRequest;
import com.insurance.dto.RegisterRequest;
import com.insurance.entity.AppUser;
import com.insurance.entity.Customer;
import com.insurance.entity.Role;
import com.insurance.enums.RoleName;
import com.insurance.repository.AppUserRepository;
import com.insurance.repository.CustomerRepository;
import com.insurance.repository.RoleRepository;
import com.insurance.security.CustomUserDetails;
import com.insurance.security.CustomUserDetailsService;
import com.insurance.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;
@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    public AuthService(AppUserRepository appUserRepository, CustomerRepository customerRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }
    @Transactional
    public AuthResponse registerClaimant(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        Role claimantRole = roleRepository.findByName(RoleName.CLAIMANT)
                .orElseThrow(() -> new IllegalStateException("CLAIMANT role is missing from the database"));
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.getRoles().add(claimantRole);
        AppUser savedUser = appUserRepository.save(user);
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
        customerRepository.save(customer);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        return createAuthResponse(userDetails);
    }
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        return createAuthResponse(userDetails);
    }
    private AuthResponse createAuthResponse(CustomUserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", "")).sorted().toList();
        return new AuthResponse(token, jwtService.getExpirationMs(), userDetails.getId(), userDetails.getUsername(),
                roles);
    }
}
