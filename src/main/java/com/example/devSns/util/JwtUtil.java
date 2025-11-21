package com.example.devSns.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET;
    private final Long EXPIRATION;
    private final Long REFRESH_EXPIRATION;
    private final SecureRandom secureRandom;


    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") Long expiration,
        @Value("${jwt.refresh_expiration}") Long refresh_expiration
    ) {
       this.SECRET = secret;
       this.EXPIRATION = expiration * 1000;
       this.REFRESH_EXPIRATION = refresh_expiration ;
       this.secureRandom = new SecureRandom();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public Long getRefreshExpiration() {
        return REFRESH_EXPIRATION;

    }

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    public byte[] generateRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
