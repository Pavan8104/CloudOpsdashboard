package com.cloudops.dashboard.service;

import com.cloudops.dashboard.dto.LoginRequest;
import com.cloudops.dashboard.dto.LoginResponse;
import com.cloudops.dashboard.model.Role;
import com.cloudops.dashboard.model.User;
import com.cloudops.dashboard.repository.UserRepository;
import com.cloudops.dashboard.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Authentication service - login, registration, aur user management yahan hota hai.
 *
 * Controller se directly AuthenticationManager call karna avoid karte hain - service layer mein rakho.
 * Last login time update karte hain - security audit ke liye useful hai yeh information.
 * Password encoding seedha yahan hota hai - controller ko nahi pata kaise encode hua.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Login - credentials verify karo aur JWT token return karo.
     * Spring AuthenticationManager handle karta hai actual verification.
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        // Spring Security se authenticate karo - yeh UserDetailsService call karega
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // Authentication successful - Security Context mein set karo
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT token generate karo
        String jwt = tokenProvider.generateToken(authentication);

        // Last login time update karo - audit trail ke liye
        userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername())
            .ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("User logged in successfully: {}", user.getUsername());
            });

        // User info load karo response ke liye
        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsername(), loginRequest.getUsername())
            .orElseThrow();

        return LoginResponse.builder()
            .accessToken(jwt)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getExpirationMs())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .roles(user.getRoles())
            .build();
    }

    /**
     * New user register karo - default role VIEWER dete hain.
     * Admin apne aap nahi ban sakta - role manually assign karna padega.
     */
    public User registerUser(String username, String email, String password, String fullName) {
        // Check karo duplicate nahi hai
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' already le gaya koi - doosra choose karo");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Is email se already account hai");
        }

        User newUser = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))  // Encode karo - plaintext kabhi nahi
            .fullName(fullName)
            .roles(Set.of(Role.ROLE_VIEWER))  // Default role - safe start
            .enabled(true)
            .build();

        User savedUser = userRepository.save(newUser);
        log.info("New user registered: {} with role VIEWER", username);

        return savedUser;
    }

    /**
     * Admin user ko specific role assign karo.
     * Sirf ADMIN hi yeh kar sakta hai - controller mein @PreAuthorize se enforce hoga.
     */
    public User assignRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.getRoles().add(role);
        User updated = userRepository.save(user);
        log.info("Role {} assigned to user: {}", role, user.getUsername());

        return updated;
    }

    /**
     * User enable/disable karo - delete karne se better hai, history maintain rehti hai.
     */
    public User toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setEnabled(enabled);
        User updated = userRepository.save(user);
        log.info("User {} status changed to: {}", user.getUsername(), enabled ? "ENABLED" : "DISABLED");

        return updated;
    }
}
