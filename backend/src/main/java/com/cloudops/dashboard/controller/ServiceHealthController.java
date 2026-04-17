package com.cloudops.dashboard.controller;

import com.cloudops.dashboard.dto.ServiceHealthDTO;
import com.cloudops.dashboard.model.ServiceHealth.HealthStatus;
import com.cloudops.dashboard.service.ServiceHealthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ServiceHealth Controller - service monitoring ke REST endpoints.
 *
 * GET endpoints sabke liye (VIEWER, ENGINEER, ADMIN).
 * Write endpoints sirf ENGINEER aur ADMIN ke liye.
 * DELETE sirf ADMIN kar sakta hai - SecurityConfig mein bhi set hai but yahan double check.
 * Sab responses consistent format mein hain - DTO wapas karo seedha.
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceHealthController {

    private final ServiceHealthService serviceHealthService;

    /**
     * GET /api/services - sabhi monitored services ki list.
     * Dashboard main table yahan se data leta hai.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<ServiceHealthDTO>> getAllServices() {
        return ResponseEntity.ok(serviceHealthService.getAllServices());
    }

    /**
     * GET /api/services/{id} - specific service ki details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<ServiceHealthDTO> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceHealthService.getServiceById(id));
    }

    /**
     * GET /api/services/status/{status} - status ke basis pe filter.
     * DOWN services ki urgent list ke liye kaafi use hota hai yeh endpoint.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<ServiceHealthDTO>> getServicesByStatus(
            @PathVariable HealthStatus status) {
        return ResponseEntity.ok(serviceHealthService.getServicesByStatus(status));
    }

    /**
     * GET /api/services/summary - dashboard summary widget ke liye.
     * Returns: { "UP": 10, "DOWN": 2, "DEGRADED": 1 }
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<Map<String, Long>> getStatusSummary() {
        return ResponseEntity.ok(serviceHealthService.getStatusSummary());
    }

    /**
     * POST /api/services - naya service monitoring mein add karo.
     * ENGINEER aur ADMIN kar sakte hain yeh.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<ServiceHealthDTO> createService(
            @Valid @RequestBody ServiceHealthDTO dto) {
        ServiceHealthDTO created = serviceHealthService.createOrUpdateService(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/services/{id} - service update karo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<ServiceHealthDTO> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceHealthDTO dto) {
        dto.setId(id);
        ServiceHealthDTO updated = serviceHealthService.createOrUpdateService(dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/services/{id}/status - status sirf update karo.
     * Health check system yahi call karta hai - full object update nahi chahiye.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<ServiceHealthDTO> updateServiceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> statusUpdate) {

        HealthStatus status = HealthStatus.valueOf((String) statusUpdate.get("status"));
        String message = (String) statusUpdate.get("message");
        Long responseTimeMs = statusUpdate.get("responseTimeMs") != null
            ? Long.valueOf(statusUpdate.get("responseTimeMs").toString()) : null;

        return ResponseEntity.ok(
            serviceHealthService.updateServiceStatus(id, status, message, responseTimeMs));
    }

    /**
     * DELETE /api/services/{id} - service monitoring se hata do.
     * Sirf ADMIN kar sakta hai - cautious action hai yeh.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteService(@PathVariable Long id) {
        serviceHealthService.deleteService(id);
        return ResponseEntity.ok(Map.of("message", "Service monitoring se remove kar diya"));
    }
}
