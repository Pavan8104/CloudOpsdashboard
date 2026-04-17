package com.cloudops.dashboard.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider - tokens generate karna aur validate karna yahan hota hai.
 *
 * Yeh class gate pe guard ki tarah kaam karti hai - token valid hai ya nahi,
 * expire to nahi hua, tampered to nahi kiya - sab yahan check hota hai.
 * JJWT library use kar rahe hain - industry standard aur well maintained hai.
 *
 * Security note: Secret key strong honi chahiye - production mein GCP Secret Manager use karo.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    // application.properties se aata hai ye value
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Milliseconds mein - default 24 hours
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Crypto-grade signing key banata hai secret string se.
     * HS256 ke liye minimum 256-bit key chahiye - yah ensure karta hai.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Authentication object se JWT token generate karo.
     * Token mein username aur expiry store hota hai - roles bhi dal sakte hain future mein.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userDetails.getUsername());
    }

    /**
     * Username se directly token generate karo - refresh token flow ke liye.
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .subject(username)                    // Token ka main subject - username
            .issuedAt(now)                        // Kab banaya gaya
            .expiration(expiry)                   // Kab expire hoga
            .issuer("cloudops-dashboard")         // Issuer identify karne ke liye
            .signWith(getSigningKey())            // Sign with HMAC-SHA256
            .compact();
    }

    /**
     * Token se username extract karo - filter mein yahi use hota hai.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * Token valid hai ya nahi check karo - filter har request mein yeh call karta hai.
     * Multiple failure modes handle karo - expired, malformed, unsupported, invalid signature.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token expire ho gaya - user ko logout karke re-login kahna padega
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // Token format galat hai - kisi ne tamper kiya shayad
            log.error("Invalid JWT token format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token string empty ya null hai
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            // Kuch aur problem - broad catch but log karo
            log.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Token ki expiry time miliseconds mein return karo - LoginResponse ke liye.
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
