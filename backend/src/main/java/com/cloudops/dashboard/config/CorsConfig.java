package com.cloudops.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration - Angular frontend se backend ke saath baat karne ke liye.
 *
 * Browser same-origin policy block karti hai cross-origin requests.
 * Yeh config Spring ko batata hai kaunse origins se requests allow karni hain.
 * Development mein localhost:4200, production mein actual domain hoga.
 * Bahut broad CORS mat lagao - sirf zaroori origins allow karo.
 */
@Configuration
public class CorsConfig {

    // application.properties se origins list aati hai - comma separated
    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    /**
     * CORS configuration source - Spring Security isse use karta hai.
     * Allowed origins, methods, headers sab yahan define hote hain.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origins parse karo - comma separated list ho sakti hai
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Standard HTTP methods - DELETE bhi allow karo kyunki API mein use hota hai
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Headers - Authorization sabse important hai JWT ke liye
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Cache-Control",
            "X-CSRF-Token"
        ));

        // Response headers jo browser ko expose karne hain
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",  // Pagination ke liye total count header
            "X-Page-Number",
            "X-Page-Size"
        ));

        // Credentials allow karo - cookies ya Authorization headers ke liye
        configuration.setAllowCredentials(true);

        // Preflight cache - browser 3600 seconds (1 ghante) tak preflight cache karega
        configuration.setMaxAge(3600L);

        // Sab API paths pe apply karo
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
