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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with cost factor 12 — industry standard, brute-force resistant
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Security headers — defense in depth
            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self' https://cloudops-dashboard.onrender.com; " +
                    "frame-ancestors 'none'; " +
                    "form-action 'self';"
                ));
                headers.referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                headers.permissionsPolicy(pp -> pp.policy("camera=(), microphone=(), geolocation=(), payment=(), usb=()"));
                headers.frameOptions(fo -> fo.deny());
                headers.httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true).preload(true));
            })

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/services/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/services/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.PUT, "/services/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.DELETE, "/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/incidents/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/incidents/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.PUT, "/incidents/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers(HttpMethod.DELETE, "/incidents/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/resources/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .requestMatchers("/resources/**").hasAnyRole("ADMIN", "ENGINEER")
                .requestMatchers("/chatbot/**").hasAnyRole("ADMIN", "ENGINEER", "VIEWER")
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
