package com.cloudops.dashboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter - har HTTP request iske through guzarti hai.
 *
 * Yeh filter Authorization header se Bearer token nikaalta hai,
 * validate karta hai, aur Security Context mein authentication set karta hai.
 * OncePerRequestFilter extend kiya hai - ek request mein sirf ek baar execute hoga.
 * Yeh Spring Security ki filter chain mein UsernamePasswordAuthenticationFilter se pehle aata hai.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Har request pe yeh method call hoti hai - token extract, validate, authenticate.
     * Agar token nahi hai ya invalid hai toh silently bypass - error nahi throw karte,
     * Spring Security baad mein unauthorized response dega khud.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Header se token nikalo
            String jwt = extractTokenFromRequest(request);

            // Token hai aur valid bhi hai - authentication set karo
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);

                // Database se user load karo - roles ke saath
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Authentication object banao aur Security Context mein set karo
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,  // Credentials null - already authenticated via token
                        userDetails.getAuthorities()
                    );

                // Request details attach karo - IP address etc. audit ke liye
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Security Context mein store karo - yahan se poori application access kar sakti hai
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", username);
            }
        } catch (Exception e) {
            // Koi bhi exception silently ignore - request aage jaane do, Spring Security handle karega
            log.error("Could not set user authentication in security context: {}", e.getMessage());
        }

        // Next filter ko request pass karo - chain continue
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization header se JWT token extract karo.
     * Standard format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     * "Bearer " prefix (7 characters) remove karke token return karo.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " ke baad wala part - actual JWT token
            return bearerToken.substring(7);
        }

        return null;
    }
}
