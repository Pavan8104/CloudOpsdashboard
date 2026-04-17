package com.cloudops.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request DTO - user jo credentials bhejta hai woh yahan map hote hain.
 *
 * Seedha User entity use nahi karte API mein - DTO pattern follow karo.
 * Validation annotations yahan hi lagate hain taaki controller clean rahe.
 */
@Data
public class LoginRequest {

    // Username ya email dono se login allow kar sakte hain - flexible
    @NotBlank(message = "Username field khali nahi hona chahiye")
    private String username;

    @NotBlank(message = "Password toh dena hi padega bhai")
    private String password;
}
