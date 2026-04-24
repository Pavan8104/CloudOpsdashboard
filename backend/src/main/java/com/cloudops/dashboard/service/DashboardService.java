package com.cloudops.dashboard.service;

import com.cloudops.dashboard.dto.DashboardSummaryDTO;
import com.cloudops.dashboard.dto.IncidentDTO;
import com.cloudops.dashboard.dto.ResourceUsageDTO;
import com.cloudops.dashboard.dto.ServiceHealthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Dashboard Service - Consolidates data from multiple services for the UI.
 * 
 * This service acts as an aggregator, calling ServiceHealth, Incident,
 * and ResourceUsage services to build a single DashboardSummaryDTO.
 * This pattern avoids the N+1 API call problem on the frontend.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ServiceHealthService serviceHealthService;
    private final IncidentService incidentService;
    private final ResourceUsageService resourceUsageService;

    /**
     * Aggregates all data required for the main dashboard view.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        log.debug("Building consolidated dashboard summary...");

        // 1. Fetch Service Health data
        Map<String, Long> statusSummary = serviceHealthService.getStatusSummary();
        List<ServiceHealthDTO> allServices = serviceHealthService.getAllServices();

        // 2. Fetch Incident data
        List<IncidentDTO> criticalIncidents = incidentService.getCriticalIncidents();
        List<IncidentDTO> activeIncidents = incidentService.getActiveIncidents();

        // 3. Fetch Resource metrics data
        List<ResourceUsageDTO> alertingResources = resourceUsageService.getAlertingResources();
        List<ResourceUsageDTO> latestMetrics = resourceUsageService.getLatestMetricsAllServices();

        // 4. Build and return consolidated DTO
        return DashboardSummaryDTO.builder()
                .statusSummary(statusSummary)
                .allServices(allServices)
                .criticalIncidents(criticalIncidents)
                .activeIncidents(activeIncidents)
                .alertingResources(alertingResources)
                .latestMetrics(latestMetrics)
                .totalServices(allServices.size())
                .activeIncidentCount(activeIncidents.size())
                .alertCount(alertingResources.size())
                .build();
    }
}
