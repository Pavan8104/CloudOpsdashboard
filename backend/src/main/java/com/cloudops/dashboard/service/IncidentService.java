package com.cloudops.dashboard.service;

import com.cloudops.dashboard.dto.IncidentDTO;
import com.cloudops.dashboard.exception.ResourceNotFoundException;
import com.cloudops.dashboard.model.Incident;
import com.cloudops.dashboard.model.Incident.IncidentStatus;
import com.cloudops.dashboard.model.Incident.Severity;
import com.cloudops.dashboard.model.ServiceHealth;
import com.cloudops.dashboard.model.User;
import com.cloudops.dashboard.repository.IncidentRepository;
import com.cloudops.dashboard.repository.ServiceHealthRepository;
import com.cloudops.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Incident Service - incident lifecycle management ka main service.
 *
 * Incident create karna, update karna, resolve karna - sab yahan hota hai.
 * Incident number auto-generate hota hai - INC-20240101-001 format mein.
 * Status transitions validate karte hain - CLOSED incident reopen nahi ho sakta directly.
 * MTTR statistics bhi yahan calculate hoti hain.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ServiceHealthRepository serviceHealthRepository;
    private final UserRepository userRepository;

    // Thread-safe counter for incident numbers - restart pe reset hoga, production mein DB sequence use karo
    private final AtomicLong incidentCounter = new AtomicLong(0);

    /**
     * Naya incident create karo - SEV1 incidents ke liye extra logging.
     */
    public IncidentDTO createIncident(IncidentDTO dto, String createdByUsername) {
        User creator = userRepository.findByUsernameOrEmail(createdByUsername, createdByUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", createdByUsername));

        Incident incident = new Incident();
        incident.setTitle(dto.getTitle());
        incident.setDescription(dto.getDescription());
        incident.setSeverity(dto.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedBy(creator);
        incident.setAffectedUsersCount(dto.getAffectedUsersCount());
        incident.setStartedAt(dto.getStartedAt() != null ? dto.getStartedAt() : LocalDateTime.now());

        // Affected service link karo agar diya hai
        if (dto.getAffectedServiceId() != null) {
            serviceHealthRepository.findById(dto.getAffectedServiceId())
                .ifPresent(incident::setAffectedService);
        }

        // Unique incident number generate karo
        incident.setIncidentNumber(generateIncidentNumber());

        Incident saved = incidentRepository.save(incident);

        // SEV1 incidents pe loud logging - real system mein PagerDuty/OpsGenie alert yahan fire hota
        if (saved.getSeverity() == Severity.SEV1) {
            log.error("🚨 SEV1 INCIDENT CREATED: {} | {} | Incident: {}",
                saved.getIncidentNumber(), saved.getTitle(), saved.getId());
        } else {
            log.info("Incident created: {} - {}", saved.getIncidentNumber(), saved.getTitle());
        }

        return toDTO(saved);
    }

    /**
     * Incident update karo - status change, assignment, notes update.
     */
    public IncidentDTO updateIncident(Long id, IncidentDTO dto) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        // CLOSED incident update nahi ho sakta - reopen karo pehle
        if (incident.getStatus() == IncidentStatus.CLOSED) {
            throw new IllegalStateException("Closed incident update nahi ho sakta - pehle reopen karo");
        }

        if (dto.getTitle() != null) incident.setTitle(dto.getTitle());
        if (dto.getDescription() != null) incident.setDescription(dto.getDescription());
        if (dto.getSeverity() != null) incident.setSeverity(dto.getSeverity());
        if (dto.getResolutionNotes() != null) incident.setResolutionNotes(dto.getResolutionNotes());
        if (dto.getAffectedUsersCount() != null) incident.setAffectedUsersCount(dto.getAffectedUsersCount());

        // Status update - validate transition
        if (dto.getStatus() != null) {
            updateStatus(incident, dto.getStatus());
        }

        // Assignment update
        if (dto.getAssignedToId() != null) {
            userRepository.findById(dto.getAssignedToId())
                .ifPresent(user -> {
                    incident.setAssignedTo(user);
                    if (incident.getStatus() == IncidentStatus.OPEN) {
                        incident.setStatus(IncidentStatus.IN_PROGRESS);
                    }
                });
        }

        Incident updated = incidentRepository.save(incident);
        log.info("Incident updated: {} - Status: {}", updated.getIncidentNumber(), updated.getStatus());

        return toDTO(updated);
    }

    /**
     * Incident ko resolve karo - MTTR automatically calculate hoga.
     */
    public IncidentDTO resolveIncident(Long id, String resolutionNotes, Long resolvedByUserId) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setResolutionNotes(resolutionNotes);

        Incident resolved = incidentRepository.save(incident);
        log.info("Incident RESOLVED: {} | MTTR: {} minutes",
            resolved.getIncidentNumber(),
            java.time.Duration.between(resolved.getStartedAt(), resolved.getResolvedAt()).toMinutes());

        return toDTO(resolved);
    }

    /**
     * Sabhi incidents - filtered list.
     */
    @Transactional(readOnly = true)
    public List<IncidentDTO> getAllIncidents() {
        return incidentRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Active incidents - on-call dashboard ke liye.
     */
    @Transactional(readOnly = true)
    public List<IncidentDTO> getActiveIncidents() {
        return incidentRepository.findByStatusIn(
                List.of(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS, IncidentStatus.MONITORING))
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Critical incidents (SEV1, SEV2) - dashboard alert widget ke liye.
     */
    @Transactional(readOnly = true)
    public List<IncidentDTO> getCriticalIncidents() {
        return incidentRepository.findActiveCriticalIncidents()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentDTO getIncidentById(Long id) {
        return toDTO(incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id)));
    }

    public void deleteIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        incidentRepository.delete(incident);
        log.info("Incident deleted: {}", incident.getIncidentNumber());
    }

    /**
     * Status transition validate karo - invalid transitions reject karo.
     */
    private void updateStatus(Incident incident, IncidentStatus newStatus) {
        IncidentStatus current = incident.getStatus();

        // CLOSED se direct kisi bhi status mein nahi ja sakte
        if (current == IncidentStatus.CLOSED && newStatus != IncidentStatus.OPEN) {
            throw new IllegalStateException(
                String.format("Invalid status transition: %s -> %s", current, newStatus));
        }

        incident.setStatus(newStatus);

        // Resolve hote waqt timestamp set karo
        if (newStatus == IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
            incident.setResolvedAt(LocalDateTime.now());
        }
    }

    /**
     * Unique incident number generate karo - date + sequence format.
     * INC-YYYYMMDD-NNNN format use karte hain - sortable aur readable.
     */
    private String generateIncidentNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = incidentRepository.count() + 1;
        return String.format("INC-%s-%04d", date, count);
    }

    /**
     * Entity to DTO - nested objects ko flatten karo response ke liye.
     */
    private IncidentDTO toDTO(Incident entity) {
        IncidentDTO dto = new IncidentDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setSeverity(entity.getSeverity());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setResolutionNotes(entity.getResolutionNotes());
        dto.setAffectedUsersCount(entity.getAffectedUsersCount());
        dto.setIncidentNumber(entity.getIncidentNumber());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Nested objects flatten karo - frontend simple data chahta hai
        if (entity.getAffectedService() != null) {
            dto.setAffectedServiceId(entity.getAffectedService().getId());
            dto.setAffectedServiceName(entity.getAffectedService().getServiceName());
        }

        if (entity.getAssignedTo() != null) {
            dto.setAssignedToId(entity.getAssignedTo().getId());
            dto.setAssignedToUsername(entity.getAssignedTo().getUsername());
            dto.setAssignedToFullName(entity.getAssignedTo().getFullName());
        }

        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByUsername(entity.getCreatedBy().getUsername());
        }

        return dto;
    }
}
