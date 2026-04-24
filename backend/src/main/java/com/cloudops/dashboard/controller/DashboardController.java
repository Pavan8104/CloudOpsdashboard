package com.cloudops.dashboard.controller;

import com.cloudops.dashboard.dto.DashboardSummaryDTO;
import com.cloudops.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard Controller - One-stop API for the main dashboard view.
 * 
 * Provides a consolidated summary of all cloud operations metrics.
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        log.info("Fetching consolidated dashboard summary");
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
