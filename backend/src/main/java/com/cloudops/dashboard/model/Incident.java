package com.cloudops.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Incident entity - production incidents track karne ke liye.
 *
 * Jab koi service down hoti hai ya koi issue aata hai toh incident create hota hai.
 * Is model mein poora incident lifecycle track hota hai - creation se resolution tak.
 * Severity levels SEV1-SEV4 follow karte hain - Google ke internal incident framework jaisa.
 */
@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Incident title - short aur descriptive hona chahiye
    @NotBlank(message = "Incident title dena zaroori hai")
    @Column(nullable = false, length = 200)
    private String title;

    // Detailed description - kya hua, kaise hua, impact kya hai
    @Column(columnDefinition = "TEXT")
    private String description;

    // Severity - kitna critical hai yeh incident
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Severity severity;

    // Current status of the incident - lifecycle track karne ke liye
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.OPEN;

    // Kaun se service pe incident hai - ek incident multiple services affect kar sakta hai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceHealth affectedService;

    // Incident kaun handle kar raha hai - incident commander
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // Incident kisne create kiya - usually alert system ya on-call engineer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // Incident start time - MTTR calculate karne ke liye yeh important hai
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // Kab resolve hua - MTTR = resolvedAt - startedAt
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Resolution notes - postmortem ke liye kya kiya tha yeh likho
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // Kitne users/systems affected hue - impact tracking ke liye
    @Column(name = "affected_users_count")
    private Integer affectedUsersCount;

    // Incident number - INC-001, INC-002 format mein - human readable tracking
    @Column(name = "incident_number", unique = true, length = 20)
    private String incidentNumber;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Jab status RESOLVED ho jaye toh automatically resolvedAt set karo
        if (status == IncidentStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }

    /**
     * Severity levels - SEV1 sabse critical hai, SEV4 minor issue.
     * Google incident management framework ke according define kiye hain.
     */
    public enum Severity {
        SEV1,   // Critical - production completely down, immediate action required!
        SEV2,   // Major - significant impact, escalate karo
        SEV3,   // Minor - partial impact, fix within hours
        SEV4    // Informational - minor glitch, fix in next sprint
    }

    /**
     * Incident lifecycle states
     */
    public enum IncidentStatus {
        OPEN,           // Naya incident, koi handle nahi kar raha abhi
        IN_PROGRESS,    // Koi actively kaam kar raha hai iss pe
        MONITORING,     // Fix deploy ho gayi, monitoring kar rahe hain ki sab theek hai
        RESOLVED,       // Problem solve ho gayi
        CLOSED          // Fully closed, postmortem bhi ho gayi
    }
}
