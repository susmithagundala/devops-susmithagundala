package com.insurance.security;
import com.insurance.entity.AppUser;
import com.insurance.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
public class CustomUserDetails implements UserDetails {
    private final AppUser user;
    private final List<GrantedAuthority> authorities;
    public CustomUserDetails(AppUser user) {
        this.user = user;
        this.authorities = user.getRoles().stream().map(Role::getName)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
    }
    public Long getId() {
        return user.getId();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getEnabled());
    }
}
