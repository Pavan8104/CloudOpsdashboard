package com.cloudops.dashboard.service;

import com.cloudops.dashboard.dto.ResourceUsageDTO;
import com.cloudops.dashboard.exception.ResourceNotFoundException;
import com.cloudops.dashboard.model.ResourceUsage;
import com.cloudops.dashboard.model.ResourceUsage.ResourceType;
import com.cloudops.dashboard.repository.ResourceUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Resource Usage Service - metrics collection aur querying ka service.
 *
 * Real deployment mein yeh Google Cloud Monitoring API se data pull karta.
 * Abhi ke liye demo data generate karta hai - charts mein kuch dikhne ke liye.
 * Time range queries zyada use hoti hain - last hour, last 24 hours etc.
 * Alert threshold check bhi yahan hota hai.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceUsageService {

    private final ResourceUsageRepository resourceUsageRepository;

    // Demo data ke liye - production mein hata dena
    private final Random random = new Random();

    /**
     * Naya metric record save karo.
     */
    public ResourceUsageDTO recordMetric(ResourceUsageDTO dto) {
        ResourceUsage usage = ResourceUsage.builder()
            .serviceName(dto.getServiceName())
            .resourceType(dto.getResourceType())
            .value(dto.getValue())
            .unit(dto.getUnit())
            .maxCapacity(dto.getMaxCapacity())
            .alertThreshold(dto.getAlertThreshold())
            .region(dto.getRegion())
            .zone(dto.getZone())
            .gcpProjectId(dto.getGcpProjectId())
            .recordedAt(dto.getRecordedAt() != null ? dto.getRecordedAt() : LocalDateTime.now())
            .build();

        // Alert check karo
        if (dto.getAlertThreshold() != null) {
            double utilization = dto.getMaxCapacity() != null && dto.getMaxCapacity() > 0
                ? (dto.getValue() / dto.getMaxCapacity()) * 100
                : dto.getValue();

            if (utilization >= dto.getAlertThreshold()) {
                log.warn("⚠️ ALERT: {} {} at {:.1f}% - threshold {}% exceeded!",
                    dto.getServiceName(), dto.getResourceType(), utilization, dto.getAlertThreshold());
            }
        }

        ResourceUsage saved = resourceUsageRepository.save(usage);
        log.debug("Metric recorded: {} {} = {}", saved.getServiceName(), saved.getResourceType(), saved.getValue());

        return toDTO(saved);
    }

    /**
     * Ek service ke latest metrics - dashboard widget ke liye.
     */
    @Transactional(readOnly = true)
    public List<ResourceUsageDTO> getLatestMetricsForService(String serviceName) {
        return ResourceType.values() != null
            ? java.util.Arrays.stream(ResourceType.values())
                .map(type -> resourceUsageRepository
                    .findTopByServiceNameAndResourceTypeOrderByRecordedAtDesc(serviceName, type))
                .filter(java.util.Objects::nonNull)
                .map(this::toDTO)
                .collect(Collectors.toList())
            : List.of();
    }

    /**
     * Time range ke saath metrics - chart data ke liye.
     * Start aur end time pass karo - hourly, daily views ke liye.
     */
    @Transactional(readOnly = true)
    public List<ResourceUsageDTO> getMetricsForTimeRange(
            String serviceName, ResourceType resourceType,
            LocalDateTime start, LocalDateTime end) {

        return resourceUsageRepository
            .findByServiceNameAndRecordedAtBetweenOrderByRecordedAtAsc(serviceName, start, end)
            .stream()
            .filter(r -> resourceType == null || r.getResourceType() == resourceType)
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Sabhi services ki latest metrics - overview dashboard ke liye.
     */
    @Transactional(readOnly = true)
    public List<ResourceUsageDTO> getLatestMetricsAllServices() {
        return resourceUsageRepository.findLatestMetricsForAllServices()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Alert threshold exceed kiye hue resources - alert panel ke liye.
     */
    @Transactional(readOnly = true)
    public List<ResourceUsageDTO> getAlertingResources() {
        // Last 10 minutes ke data check karo
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        return resourceUsageRepository.findResourcesExceedingThreshold(since)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Demo metrics generate karo - scheduled, har minute chalta hai.
     * PRODUCTION MEIN HATA DENA - yeh sirf demo ke liye hai!
     * Real system mein GCP Cloud Monitoring API se pull karo.
     */
    @Scheduled(fixedDelay = 60000)  // Har ek minute
    public void generateDemoMetrics() {
        String[] services = {"GKE Cluster", "Cloud SQL", "Cloud Run API", "Pub/Sub", "Load Balancer"};
        String[] regions = {"us-central1", "us-east1", "asia-south1"};

        for (String serviceName : services) {
            // CPU metric
            recordDemoMetric(serviceName, ResourceType.CPU,
                20 + random.nextDouble() * 60,  // 20-80% CPU
                "%", 100.0, 80.0,
                regions[random.nextInt(regions.length)]);

            // Memory metric
            recordDemoMetric(serviceName, ResourceType.MEMORY,
                1024 + random.nextDouble() * 6144,  // 1-7 GB
                "MB", 8192.0, 85.0,
                regions[random.nextInt(regions.length)]);
        }

        log.debug("Demo metrics generated for {} services", services.length);
    }

    private void recordDemoMetric(String serviceName, ResourceType type,
                                   double value, String unit,
                                   double maxCapacity, double threshold, String region) {
        ResourceUsage usage = ResourceUsage.builder()
            .serviceName(serviceName)
            .resourceType(type)
            .value(value)
            .unit(unit)
            .maxCapacity(maxCapacity)
            .alertThreshold(threshold)
            .region(region)
            .gcpProjectId("cloudops-demo-project")
            .recordedAt(LocalDateTime.now())
            .build();
        resourceUsageRepository.save(usage);
    }

    private ResourceUsageDTO toDTO(ResourceUsage entity) {
        return ResourceUsageDTO.builder()
            .id(entity.getId())
            .serviceName(entity.getServiceName())
            .resourceType(entity.getResourceType())
            .value(entity.getValue())
            .unit(entity.getUnit())
            .maxCapacity(entity.getMaxCapacity())
            .alertThreshold(entity.getAlertThreshold())
            .region(entity.getRegion())
            .zone(entity.getZone())
            .gcpProjectId(entity.getGcpProjectId())
            .recordedAt(entity.getRecordedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
