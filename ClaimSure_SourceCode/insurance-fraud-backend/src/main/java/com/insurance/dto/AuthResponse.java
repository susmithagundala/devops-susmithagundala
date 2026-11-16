package com.insurance.dto;
import java.util.List;
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String email;
    private List<String> roles;
    public AuthResponse(String accessToken, long expiresIn, Long userId, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public long getExpiresIn() {
        return expiresIn;
    }
    public Long getUserId() {
        return userId;
    }
    public String getEmail() {
        return email;
    }
    public List<String> getRoles() {
        return roles;
    }
}
