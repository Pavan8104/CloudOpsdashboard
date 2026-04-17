package com.cloudops.dashboard.security;

import com.cloudops.dashboard.model.User;
import com.cloudops.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spring Security ka UserDetailsService implementation.
 *
 * Authentication ke waqt Spring Security yahan aata hai user load karne.
 * Database se User laao, Spring ka UserDetails format mein convert karo.
 * Yeh middle layer hai humara User model aur Spring Security ke beech.
 * Transaction annotation isliye hai kyunki roles lazy loaded hain.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Username se user load karo - Spring Security yeh method automatically call karta hai.
     * Hum username ya email dono se login support karte hain - thoda extra flexibility.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Username ya email dono se try karo - user convenience ke liye
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> {
                log.warn("User not found with username/email: {}", usernameOrEmail);
                // Security best practice - don't reveal whether username or password is wrong
                return new UsernameNotFoundException("Invalid credentials");
            });

        log.debug("User loaded successfully: {}", user.getUsername());

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(getAuthorities(user))
            .accountExpired(false)
            .accountLocked(!user.isEnabled())
            .credentialsExpired(false)
            .disabled(!user.isEnabled())
            .build();
    }

    /**
     * User ke roles ko Spring Security GrantedAuthority mein convert karo.
     * ROLE_ADMIN, ROLE_ENGINEER, ROLE_VIEWER - yahi authorities ban jaati hain.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(Collectors.toList());
    }
}
