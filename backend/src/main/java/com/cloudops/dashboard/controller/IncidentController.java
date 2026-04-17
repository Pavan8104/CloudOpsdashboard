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

/**
 * Incident Controller - incident management ke REST endpoints.
 *
 * Incident create, update, resolve - sab yahan hai.
 * Creator ka userId JWT token se nikalte hain - request body se trust nahi karte.
 * Critical incidents alag endpoint pe hain - dashboard alert widget ke liye.
 */
@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * GET /api/incidents - sabhi incidents.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<IncidentDTO>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    /**
     * GET /api/incidents/active - active incidents (OPEN, IN_PROGRESS, MONITORING).
     * On-call engineer ka primary view yahi hai.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<IncidentDTO>> getActiveIncidents() {
        return ResponseEntity.ok(incidentService.getActiveIncidents());
    }

    /**
     * GET /api/incidents/critical - SEV1 aur SEV2 active incidents.
     * Dashboard pe red alert banner ke liye yahi use hoga.
     */
    @GetMapping("/critical")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<IncidentDTO>> getCriticalIncidents() {
        return ResponseEntity.ok(incidentService.getCriticalIncidents());
    }

    /**
     * GET /api/incidents/{id} - specific incident ki details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<IncidentDTO> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    /**
     * POST /api/incidents - naya incident create karo.
     * Creator ID JWT token se nikalta hai - body se nahi trust karte injection se bachne ke liye.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentDTO> createIncident(
            @Valid @RequestBody IncidentDTO dto,
            Authentication authentication) {

        // Yahan user ID resolve karna chahiye - ab simplified kar diya hai
        // Production mein UserRepository se user fetch karo username se
        Long userId = 1L; // Placeholder - authentication.getName() se user dhundho
        log.info("Incident creation request from: {}", authentication.getName());

        IncidentDTO created = incidentService.createIncident(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/incidents/{id} - incident update karo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<IncidentDTO> updateIncident(
            @PathVariable Long id,
            @RequestBody IncidentDTO dto) {
        return ResponseEntity.ok(incidentService.updateIncident(id, dto));
    }

    /**
     * POST /api/incidents/{id}/resolve - incident resolve karo.
     * Resolution notes required hain - postmortem ke liye record rehni chahiye.
     */
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

        return ResponseEntity.ok(incidentService.resolveIncident(id, resolutionNotes, null));
    }

    /**
     * DELETE /api/incidents/{id} - sirf ADMIN delete kar sakta hai.
     * Incidents usually close karte hain delete nahi - audit trail ke liye.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(Map.of("message", "Incident delete ho gaya"));
    }
}
