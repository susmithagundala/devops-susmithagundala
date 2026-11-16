package com.insurance.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;
    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must decode to at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }
    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder().subject(userDetails.getUsername()).issuedAt(now).expiration(expiry).signWith(signingKey)
                .compact();
    }
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        Claims claims = parseClaims(token);
        return userDetails.getUsername().equals(claims.getSubject()) && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }
    public long getExpirationMs() {
        return expirationMs;
    }
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
