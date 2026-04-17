package com.cloudops.dashboard.controller;

import com.cloudops.dashboard.dto.LoginRequest;
import com.cloudops.dashboard.dto.LoginResponse;
import com.cloudops.dashboard.model.User;
import com.cloudops.dashboard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth Controller - login aur registration endpoints.
 *
 * Ye endpoints public hain - SecurityConfig mein /auth/** permit all kiya hai.
 * Request validation @Valid annotation se hoti hai - automatic, clean code.
 * Sensitive information response mein mat dalo - minimal data return karo.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login - user login endpoint.
     * Successful login pe JWT token milega response mein.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register - new user registration.
     * Default VIEWER role milega - admin manually upgrade karega.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");

        User user = authService.registerUser(username, email, password, fullName);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "User successfully register ho gaya!",
            "userId", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail()
        ));
    }

    /**
     * POST /api/auth/logout - client side logout.
     * JWT stateless hai - server pe kuch nahi hota, client token delete karta hai.
     * Future mein token blacklist implement kar sakte hain.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // JWT stateless hai - server pe kuch delete karne ki zaroorat nahi
        // Client side se token remove karo localStorage/sessionStorage se
        return ResponseEntity.ok(Map.of(
            "message", "Successfully logout ho gaye - token client side se delete kar do"
        ));
    }

    /**
     * GET /api/auth/me - currently logged in user ki info.
     * Authentication required hai - token se user pata hoga.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(Map.of(
            "username", authentication.getName(),
            "roles", authentication.getAuthorities()
        ));
    }
}
