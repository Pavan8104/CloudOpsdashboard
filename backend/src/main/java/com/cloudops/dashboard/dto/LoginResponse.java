package com.cloudops.dashboard.dto;

import com.cloudops.dashboard.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Login response DTO - successful login pe yeh object return hota hai client ko.
 *
 * JWT token, user info, aur roles sab ek jagah bhejte hain.
 * Frontend yeh token localStorage mein store karta hai aur
 * har request ke saath Authorization header mein bhejta hai.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    // JWT access token - har API call mein yahi use hoga
    private String accessToken;

    // Token type - standard "Bearer" hota hai, change karne ki zaroorat nahi
    @Builder.Default
    private String tokenType = "Bearer";

    // Token kitne milliseconds mein expire hoga - frontend ke liye useful
    private Long expiresIn;

    // User details - frontend ko display ke liye chahiye
    private Long userId;
    private String username;
    private String email;
    private String fullName;

    // Roles - frontend in roles ke basis pe menu items aur routes show/hide karta hai
    private Set<Role> roles;
}
