package gov.aicte.apaar.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:AICTE_APAAR_VERIFY_PRODUCTION_KEY_2026_NEP2020_SECURE_HMAC512_SUPER_SECRET}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long validityInMilliseconds) {
        // Ensure secret is at least 32 bytes for HMAC-SHA256
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            String padded = String.format("%-32s", secret).replace(' ', '0');
            keyBytes = padded.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(Long userId, String email, String role, String fullName,
                              String institutionId, String approvalStatus) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("fullName", fullName)
                .claim("institutionId", institutionId)
                .claim("approvalStatus", approvalStatus)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getInstitutionId(String token) {
        return getClaims(token).get("institutionId", String.class);
    }
}
