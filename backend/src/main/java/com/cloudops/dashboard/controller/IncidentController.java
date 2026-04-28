package com.cloudops.dashboard.controller;

import com.cloudops.dashboard.dto.IncidentDTO;
import com.cloudops.dashboard.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<IncidentDTO>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<IncidentDTO>> getActiveIncidents() {
        return ResponseEntity.ok(incidentService.getActiveIncidents());
    }

    @GetMapping("/critical")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<IncidentDTO>> getCriticalIncidents() {
        return ResponseEntity.ok(incidentService.getCriticalIncidents());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<IncidentDTO> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentDTO> createIncident(
            @Valid @RequestBody IncidentDTO dto,
            Authentication authentication) {
        log.info("Incident creation requested by: {}", authentication.getName());
        IncidentDTO created = incidentService.createIncident(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentDTO> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentDTO dto) {
        return ResponseEntity.ok(incidentService.updateIncident(id, dto));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentDTO> resolveIncident(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String resolutionNotes = body.get("resolutionNotes");
        if (resolutionNotes == null || resolutionNotes.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Incident {} resolution initiated by: {}", id, authentication.getName());
        return ResponseEntity.ok(incidentService.resolveIncident(id, resolutionNotes, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(Map.of("message", "Incident deleted successfully"));
    }
}
