package com.cloudops.dashboard.dto;

import com.cloudops.dashboard.model.ServiceHealth.HealthStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ServiceHealth DTO - API request/response ke liye clean object.
 *
 * Entity seedha expose nahi karte - DTO use karte hain.
 * Yeh pattern lagta hai extra kaam hai but field control, versioning
 * aur security ke liye bahut important hai long term mein.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDTO {

    private Long id;

    @NotBlank(message = "Service name required")
    private String serviceName;

    private String serviceType;

    // Status - default UNKNOWN agar nahi diya toh
    @Builder.Default
    private HealthStatus status = HealthStatus.UNKNOWN;

    private String region;

    private String gcpProjectId;

    // Response time - milliseconds mein
    private Long responseTimeMs;

    private Double uptimePercentage;

    private LocalDateTime lastCheckedAt;

    private String statusMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Convenience method - UI pe badge color decide karne ke liye use hota hai
     * Green, Yellow, Red - classic traffic light system
     */
    public String getStatusColor() {
        if (status == null) return "grey";
        return switch (status) {
            case UP -> "green";
            case DEGRADED -> "yellow";
            case DOWN -> "red";
            case MAINTENANCE -> "blue";
            default -> "grey";
        };
    }
}
