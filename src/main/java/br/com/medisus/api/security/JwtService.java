package br.com.medisus.api.security;

import br.com.medisus.api.domain.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${medisus.jwt.secret}") String secret,
                      @Value("${medisus.jwt.expiration-seconds}") long expirationSeconds) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generate(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getUsername()).claim("role", user.getRole().name())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key).compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        return extractUsername(token).equalsIgnoreCase(user.getUsername());
    }

    public long getExpirationSeconds() { return expirationSeconds; }
}
