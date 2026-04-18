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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername())
            .ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Login successful for user: {}", user.getUsername());
            });

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

    public User registerUser(String username, String email, String password, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User newUser = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .fullName(fullName)
            .roles(Set.of(Role.ROLE_VIEWER))
            .enabled(true)
            .build();

        User savedUser = userRepository.save(newUser);
        log.info("New user registered: {} with default VIEWER role", username);
        return savedUser;
    }

    public User assignRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.getRoles().add(role);
        User updated = userRepository.save(user);
        log.info("Role {} assigned to user: {}", role, user.getUsername());
        return updated;
    }

    public User toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setEnabled(enabled);
        User updated = userRepository.save(user);
        log.info("User {} status changed to: {}", user.getUsername(), enabled ? "ENABLED" : "DISABLED");
        return updated;
    }
}
