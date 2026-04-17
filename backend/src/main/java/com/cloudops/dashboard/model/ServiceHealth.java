package com.cloudops.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ServiceHealth entity - har Google Cloud service ka health status yahan track hota hai.
 *
 * Jaise GKE cluster, Cloud SQL, Pub/Sub, Cloud Run services - sab ka status
 * ek jagah dekhna hota hai operations team ko. Is model mein woh sab store hoga.
 * Status values: UP, DOWN, DEGRADED, UNKNOWN - simple aur clear
 */
@Entity
@Table(name = "service_health")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Service ka naam - jaise "Cloud SQL Primary", "GKE Cluster us-central1" etc.
    @NotBlank(message = "Service name required hai")
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    // GCP service category - organize karne ke liye (Compute, Storage, Database, etc.)
    @Column(name = "service_type", length = 50)
    private String serviceType;

    // Health status - yeh important hai, iske basis pe alerts aur dashboard color change hota hai
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HealthStatus status = HealthStatus.UNKNOWN;

    // GCP region - us-central1, us-east1, asia-south1 etc.
    @Column(length = 50)
    private String region;

    // GCP project ID - multi-project setup mein kaafi useful hota hai
    @Column(name = "gcp_project_id", length = 100)
    private String gcpProjectId;

    // Response time in milliseconds - latency track karne ke liye
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    // Uptime percentage - 99.9% SLA track karne ke liye
    @Column(name = "uptime_percentage")
    private Double uptimePercentage;

    // Last time health check hua tha
    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    // Koi additional message ya error details
    @Column(name = "status_message", length = 500)
    private String statusMessage;

    // Automatic timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastCheckedAt == null) {
            lastCheckedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Health status enum - sirf yahi values allowed hain.
     * DEGRADED matlab service chal rahi hai but slowly ya partially.
     */
    public enum HealthStatus {
        UP,         // Sab theek hai, green light
        DOWN,       // Service down hai - alert fire karo!
        DEGRADED,   // Chal rahi hai but issues hain - yellow status
        UNKNOWN,    // Health check nahi hua ya timeout ho gaya
        MAINTENANCE // Planned downtime - isliye down hai, panic mat karo
    }
}
