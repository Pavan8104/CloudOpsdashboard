package com.cloudops.dashboard.dto;

import com.cloudops.dashboard.model.ResourceUsage.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ResourceUsage DTO - metrics data ke liye clean transfer object.
 *
 * Charts ke liye data yahi se aata hai. Time series queries ke results
 * list of ResourceUsageDTO ke roop mein return hote hain.
 * Utilization percentage bhi calculate karta hai yahan hi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUsageDTO {

    private Long id;

    @NotBlank
    private String serviceName;

    @NotNull
    private ResourceType resourceType;

    @NotNull
    private Double value;

    private String unit;

    private Double maxCapacity;

    private Double alertThreshold;

    private String region;

    private String zone;

    private String gcpProjectId;

    private LocalDateTime recordedAt;

    private LocalDateTime createdAt;

    /**
     * Utilization percentage calculate karta hai.
     * Agar maxCapacity pata hai toh percentage show karo, warna sirf value.
     * Chart mein 100% se zyada ho toh red highlight karna chahiye.
     */
    public Double getUtilizationPercentage() {
        if (maxCapacity == null || maxCapacity == 0) {
            // maxCapacity set nahi hua toh assume karo value already percentage mein hai
            return value;
        }
        return (value / maxCapacity) * 100.0;
    }

    /**
     * Alert threshold cross hua ya nahi - UI mein warning badge ke liye
     * True hua toh frontend red/orange color dikhaega resource card pe
     */
    public boolean isAlertTriggered() {
        if (alertThreshold == null) return false;
        return getUtilizationPercentage() >= alertThreshold;
    }

    /**
     * Human readable label - chart legend mein use hota hai
     */
    public String getDisplayLabel() {
        return serviceName + " - " + resourceType.name() + (unit != null ? " (" + unit + ")" : "");
    }
}
