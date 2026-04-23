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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final com.cloudops.dashboard.service.RateLimitingService rateLimitingService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            jakarta.servlet.http.HttpServletRequest request) {
        
        String ip = request.getRemoteAddr();
        if (!rateLimitingService.resolveBucket(ip).tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {} attempting to login as: {}", ip, loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        log.info("Login request received for: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");

        User user = authService.registerUser(username, email, password, fullName);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "User registered successfully",
            "userId", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
            "message", "Logged out successfully. Please remove the token from client storage."
        ));
    }

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
