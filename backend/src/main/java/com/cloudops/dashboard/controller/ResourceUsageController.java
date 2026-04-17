package com.cloudops.dashboard.controller;

import com.cloudops.dashboard.dto.ResourceUsageDTO;
import com.cloudops.dashboard.model.ResourceUsage.ResourceType;
import com.cloudops.dashboard.service.ResourceUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resource Usage Controller - metrics aur resource utilization endpoints.
 *
 * Charts ke liye data yahan se aata hai.
 * Time range parameters support karte hain - Angular frontend mein date picker se send hota hai.
 * Mostly read-only endpoints hain - write endpoints sirf internal/scheduled use ke liye.
 */
@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceUsageController {

    private final ResourceUsageService resourceUsageService;

    /**
     * GET /api/resources/latest - sabhi services ki latest metrics.
     * Dashboard overview widget ke liye - current utilization at a glance.
     */
    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<ResourceUsageDTO>> getLatestMetrics() {
        return ResponseEntity.ok(resourceUsageService.getLatestMetricsAllServices());
    }

    /**
     * GET /api/resources/service/{serviceName}/latest - specific service ki latest metrics.
     * Service detail page pe yeh widget use karta hai.
     */
    @GetMapping("/service/{serviceName}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<ResourceUsageDTO>> getLatestMetricsForService(
            @PathVariable String serviceName) {
        return ResponseEntity.ok(resourceUsageService.getLatestMetricsForService(serviceName));
    }

    /**
     * GET /api/resources/service/{serviceName}/history - time range ke saath metrics.
     * Line chart ke liye data yahan se aata hai.
     *
     * Query params:
     * - resourceType: CPU, MEMORY, DISK etc. (optional - sab types chahiye toh mat do)
     * - start: ISO datetime (required)
     * - end: ISO datetime (optional, default = now)
     */
    @GetMapping("/service/{serviceName}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<ResourceUsageDTO>> getMetricsHistory(
            @PathVariable String serviceName,
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime endTime = end != null ? end : LocalDateTime.now();

        return ResponseEntity.ok(
            resourceUsageService.getMetricsForTimeRange(serviceName, resourceType, start, endTime));
    }

    /**
     * GET /api/resources/alerts - threshold exceed kiye hue resources.
     * Alert panel ke liye - kaunsi services trouble mein hain abhi.
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<List<ResourceUsageDTO>> getAlertingResources() {
        return ResponseEntity.ok(resourceUsageService.getAlertingResources());
    }

    /**
     * POST /api/resources/record - new metric record karo.
     * Yeh endpoint external systems call karte hain metrics push karne ke liye.
     * API key ya service account authentication add karo production mein.
     */
    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ResponseEntity<ResourceUsageDTO> recordMetric(
            @RequestBody ResourceUsageDTO dto) {
        return ResponseEntity.ok(resourceUsageService.recordMetric(dto));
    }
}
