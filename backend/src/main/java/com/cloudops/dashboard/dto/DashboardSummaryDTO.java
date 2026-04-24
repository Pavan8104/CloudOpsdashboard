package com.cloudops.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DashboardSummaryDTO - One-stop object for the entire dashboard UI.
 * 
 * Instead of making 6 separate API calls, the Angular dashboard will
 * hit one endpoint and get this consolidated object. This solves the
 * N+1 API fetching problem and reduces latency and server load.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    // Service Health data
    private Map<String, Long> statusSummary;
    private List<ServiceHealthDTO> allServices;

    // Incident data
    private List<IncidentDTO> criticalIncidents;
    private List<IncidentDTO> activeIncidents;

    // Resource metrics data
    private List<ResourceUsageDTO> alertingResources;
    private List<ResourceUsageDTO> latestMetrics;

    // Metadata
    private long totalServices;
    private int activeIncidentCount;
    private int alertCount;
}
