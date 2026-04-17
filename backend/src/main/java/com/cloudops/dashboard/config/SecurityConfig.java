package com.cloudops.dashboard.config;

import com.cloudops.dashboard.security.JwtAuthenticationFilter;
import com.cloudops.dashboard.security.JwtTokenProvider;
import com.cloudops.dashboard.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security ka main configuration - yahan sab kuch wired hota hai.
 *
 * JWT based stateless authentication use kar rahe hain - no sessions.
 * RBAC ke liye @EnableMethodSecurity hai - @PreAuthorize annotations kaam karenge controllers mein.
 * CSRF disable kiya hai kyunki REST API hai aur JWT use kar rahe hain - sessions nahi.
 *
 * Public endpoints: /auth/**, /actuator/health - baki sab protected hain.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // @PreAuthorize, @PostAuthorize enable karta hai
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Password encoder - BCrypt industry standard hai, cost factor 10 by default.
     * Kabhi MD5 ya SHA-1 use mat karo - insecure hain.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT filter bean - Spring ke paas inject hoga SecurityFilterChain mein.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    /**
     * Authentication provider - database se users load karta hai aur password verify karta hai.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager - service layer mein authentication ke liye inject hoga.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main security filter chain - yahan sab authorization rules define hote hain.
     * Ye method thoda lamba hai but sab kuch ek jagah clear dikhai deta hai.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disable - REST API + JWT use kar rahe hain, sessions nahi
            .csrf(AbstractHttpConfigurer::disable)

            // CORS enable - frontend se requests allow karne ke liye (CorsConfig se config aayegi)
            .cors(cors -> cors.configure(http))

            // Stateless sessions - har request self-contained hai JWT se
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules - order matters! Specific pehle, generic baad mein
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints - sab ke liye public
                .requestMatchers("/auth/**").permitAll()

                // Actuator health probes - GCP ke liye public
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()

                // H2 console - sirf local dev ke liye (production mein disable hoga)
                .requestMatchers("/h2-console/**").permitAll()

                // Service health - GET sirf VIEWER+ ke liye, POST/PUT/DELETE ADMIN/ENGINEER ke liye
                .requestMatchers(HttpMethod.GET, "/services/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/services/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.PUT, "/services/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.DELETE, "/services/**").hasRole("ADMIN")

                // Incidents - VIEWER sirf dekh sakta hai
                .requestMatchers(HttpMethod.GET, "/incidents/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/incidents/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.PUT, "/incidents/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.DELETE, "/incidents/**").hasRole("ADMIN")

                // Resource usage - read-only for VIEWER
                .requestMatchers(HttpMethod.GET, "/resources/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .requestMatchers("/resources/**").hasAnyRole("ADMIN", "ENGINEER")

                // Baki sab authenticated users ke liye
                .anyRequest().authenticated()
            )

            // Authentication provider set karo
            .authenticationProvider(authenticationProvider())

            // JWT filter add karo - UsernamePasswordAuthenticationFilter se PEHLE
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // H2 console ke liye frame options allow karo - sirf dev mein
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
