package com.cloudops.dashboard.dto;

import com.cloudops.dashboard.model.Incident.IncidentStatus;
import com.cloudops.dashboard.model.Incident.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * IncidentDTO - incident data transfer object for APIs.
 *
 * Create, Update, aur Read - sab ke liye yeh ek hi DTO use hota hai.
 * MTTR calculation bhi yahan hoti hai taaki frontend ko sab ready-made mile.
 * Nested user info flatten kar ke dete hain - complex objects return karne se avoid karo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {

    private Long id;

    @NotBlank(message = "Incident title dena zaroori hai - kya hua yeh batao")
    @Size(max = 200, message = "Title too long")
    private String title;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Severity toh batao - SEV1 se SEV4 mein se koi ek")
    private Severity severity;

    @Builder.Default
    private IncidentStatus status = IncidentStatus.OPEN;

    // Affected service ki basic info - full nested object nahi bhejte
    private Long affectedServiceId;
    private String affectedServiceName;

    // Assigned engineer ki info
    private Long assignedToId;
    private String assignedToUsername;
    private String assignedToFullName;

    // Creator info
    private Long createdById;
    private String createdByUsername;

    private LocalDateTime startedAt;
    private LocalDateTime resolvedAt;

    @Size(max = 2000, message = "Resolution notes too long")
    private String resolutionNotes;
    private Integer affectedUsersCount;
    private String incidentNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * MTTR - Mean Time To Resolve calculate karta hai.
     * Agar resolve nahi hua toh abhi tak ka time return karta hai.
     * Ops metrics ke liye yeh number bahut important hota hai.
     */
    public String getMttr() {
        if (startedAt == null) return "N/A";

        LocalDateTime endTime = (resolvedAt != null) ? resolvedAt : LocalDateTime.now();
        Duration duration = Duration.between(startedAt, endTime);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    /**
     * Severity ke basis pe badge color - UI pe color coding ke liye
     * SEV1 = red (critical), SEV2 = orange, SEV3 = yellow, SEV4 = blue (informational)
     */
    public String getSeverityColor() {
        if (severity == null) return "grey";
        return switch (severity) {
            case SEV1 -> "red";
            case SEV2 -> "orange";
            case SEV3 -> "yellow";
            case SEV4 -> "blue";
        };
    }
}
