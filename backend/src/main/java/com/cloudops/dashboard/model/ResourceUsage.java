package com.cloudops.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ResourceUsage entity - GCP resource consumption track karne ke liye.
 *
 * CPU, Memory, Disk, Network - sab kuch yahan stored hota hai time series format mein.
 * Yeh data charts pe show hota hai dashboard mein. Har service ke liye
 * regular intervals pe metrics collect hote hain aur yahan store hote hain.
 * Future mein Cloud Monitoring API se directly pull kar sakte hain.
 */
@Entity
@Table(name = "resource_usage",
    indexes = {
        // Frequently query karte hain time range pe - isliye index zaroori hai
        @Index(name = "idx_resource_usage_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_resource_usage_service_name", columnList = "service_name")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kis service ka resource usage hai - GKE, Cloud Run, Compute Engine etc.
    @NotBlank
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    // Resource type - CPU, MEMORY, DISK, NETWORK_IN, NETWORK_OUT
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    // Current usage value - CPU ke liye percentage, Memory ke liye MB/GB
    @Column(name = "metric_value", nullable = false)
    private Double value;

    // Unit - PERCENT, MB, GB, MBPS, REQUESTS_PER_SEC etc.
    @Column(length = 20)
    private String unit;

    // Maximum available capacity - utilization % calculate karne ke liye
    @Column(name = "max_capacity")
    private Double maxCapacity;

    // Threshold - iss se zyada ho toh alert fire hoga
    @Column(name = "alert_threshold")
    private Double alertThreshold;

    // GCP region aur zone
    @Column(length = 50)
    private String region;

    @Column(length = 50)
    private String zone;

    // GCP project ID
    @Column(name = "gcp_project_id", length = 100)
    private String gcpProjectId;

    // Metric record karne ka time - time series ke liye yeh primary ordering field hai
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    // Metadata - extra info store karne ke liye JSON string (instance ID, labels etc.)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    /**
     * Supported resource types - GCP ke main metrics yahi hain.
     * Baad mein custom metrics bhi add kar sakte hain.
     */
    public enum ResourceType {
        CPU,            // CPU utilization in percentage
        MEMORY,         // Memory usage in MB or percentage
        DISK,           // Disk usage in GB or percentage
        NETWORK_IN,     // Inbound network traffic in Mbps
        NETWORK_OUT,    // Outbound network traffic in Mbps
        REQUESTS,       // HTTP requests per second
        ERROR_RATE,     // Error percentage - 5xx responses
        LATENCY         // P50/P95/P99 latency in milliseconds
    }
}
