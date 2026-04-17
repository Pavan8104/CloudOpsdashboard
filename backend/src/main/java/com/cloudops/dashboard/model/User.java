package com.cloudops.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity - CloudOps ke saare users yahan stored hain.
 *
 * Ek user ke multiple roles ho sakte hain - ManyToMany relation hai.
 * Password bcrypt encoded hoga - plaintext kabhi store mat karo (obviously!).
 * Google Cloud ke saath SSO future mein add karna hai - placeholder fields hain.
 */
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Auto-generated ID - sequence strategy PostgreSQL ke liye better hai
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username unique hona chahiye - login mein yahi use hota hai
    @NotBlank(message = "Username khali nahi hona chahiye")
    @Size(min = 3, max = 50, message = "Username 3-50 characters ke beech hona chahiye")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Email bhi unique - forgot password flow ke liye zaroori
    @NotBlank
    @Email(message = "Valid email address do bhai")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt encoded password - @JsonProperty(access = WRITE_ONLY) taaki API response mein na aaye
    @NotBlank
    @Size(min = 6, message = "Password kam se kam 6 characters ka hona chahiye")
    @Column(nullable = false)
    private String password;

    // User ka full display name - dashboard pe dikhai deta hai
    @Column(name = "full_name", length = 100)
    private String fullName;

    // Roles - ManyToMany because ek user multiple roles le sakta hai
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Account enabled/disabled - user ko delete karne ki jagah disable karo (audit trail ke liye)
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Timestamps - audit ke liye, kaun kab aaya yeh track karte hain
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Last login track karte hain - security monitoring ke liye useful
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Google Cloud SSO ke liye - future feature, abhi null rehega
    @Column(name = "google_id")
    private String googleId;

    // JPA lifecycle hooks - timestamps automatically set hote hain
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        // Har update pe timestamp refresh karo - manually set karne ki zaroorat nahi
        updatedAt = LocalDateTime.now();
    }
}
