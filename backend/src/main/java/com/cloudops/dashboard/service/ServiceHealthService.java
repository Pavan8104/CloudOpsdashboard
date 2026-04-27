package com.cloudops.dashboard.service;

import com.cloudops.dashboard.dto.ServiceHealthDTO;
import com.cloudops.dashboard.exception.ResourceNotFoundException;
import com.cloudops.dashboard.model.ServiceHealth;
import com.cloudops.dashboard.model.ServiceHealth.HealthStatus;
import com.cloudops.dashboard.repository.ServiceHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service Health Service - GCP services ki health monitoring ka brain.
 *
 * CRUD operations ke alawa scheduled health checks bhi hain yahan.
 * Entity se DTO conversion yahan hoti hai - mapper library use nahi ki,
 * manual conversion clear hai aur dependencies kam hain.
 * @Scheduled method real deployment mein actual HTTP health check karega services pe.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceHealthService {

    private final ServiceHealthRepository serviceHealthRepository;

    /**
     * Sabhi services ki health list - dashboard main widget ke liye.
     */
    @Transactional(readOnly = true)
    public List<ServiceHealthDTO> getAllServices() {
        return serviceHealthRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * ID se specific service health fetch karo.
     */
    @Transactional(readOnly = true)
    public ServiceHealthDTO getServiceById(Long id) {
        ServiceHealth service = serviceHealthRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        return toDTO(service);
    }

    /**
     * Naya service add karo monitoring mein.
     * Service pehle se exist karti ho toh update karo - upsert behavior.
     */
    public ServiceHealthDTO createOrUpdateService(ServiceHealthDTO dto) {
        // Check karo same naam ka service already hai ya nahi
        ServiceHealth service = serviceHealthRepository.findByServiceName(dto.getServiceName())
            .orElse(new ServiceHealth());

        service.setServiceName(dto.getServiceName());
        service.setServiceType(dto.getServiceType());
        service.setStatus(dto.getStatus() != null ? dto.getStatus() : HealthStatus.UNKNOWN);
        service.setRegion(dto.getRegion());
        service.setGcpProjectId(dto.getGcpProjectId());
        service.setResponseTimeMs(dto.getResponseTimeMs());
        service.setUptimePercentage(dto.getUptimePercentage());
        service.setStatusMessage(dto.getStatusMessage());
        service.setLastCheckedAt(LocalDateTime.now());

        ServiceHealth saved = serviceHealthRepository.save(service);
        log.info("Service health updated: {} -> {}", saved.getServiceName(), saved.getStatus());

        return toDTO(saved);
    }

    /**
     * Service ka status update karo - health check ke baad yahi call hota hai.
     */
    public ServiceHealthDTO updateServiceStatus(Long id, HealthStatus status, String message, Long responseTimeMs) {
        ServiceHealth service = serviceHealthRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        HealthStatus oldStatus = service.getStatus();
        service.setStatus(status);
        service.setStatusMessage(message);
        service.setResponseTimeMs(responseTimeMs);
        service.setLastCheckedAt(LocalDateTime.now());

        ServiceHealth saved = serviceHealthRepository.save(service);

        // Status change hua toh log karo - alerting system ke liye event publish kar sakte hain
        if (oldStatus != status) {
            log.warn("Service status changed: {} | {} -> {}", service.getServiceName(), oldStatus, status);
        }

        return toDTO(saved);
    }

    /**
     * Service delete karo - monitoring se remove karo.
     */
    public void deleteService(Long id) {
        ServiceHealth service = serviceHealthRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        serviceHealthRepository.delete(service);
        log.info("Service removed from monitoring: {}", service.getServiceName());
    }

    /**
     * Status ke basis pe filter - DOWN services ka quick view.
     */
    @Transactional(readOnly = true)
    public List<ServiceHealthDTO> getServicesByStatus(HealthStatus status) {
        return serviceHealthRepository.findByStatus(status)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Dashboard summary - kitni UP, DOWN, DEGRADED - widget ke liye.
     * Map<Status, Count> format mein return karta hai.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusSummary() {
        return serviceHealthRepository.getStatusSummary()
            .stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
    }

    /**
     * Scheduled health check - har 5 minute mein automatically chalta hai.
     * Real HTTP ping based health check implementation.
     */
    @Scheduled(fixedDelayString = "${health.check.interval:300000}")
    public void performScheduledHealthChecks() {
        log.debug("Running scheduled health checks for all services...");
        List<ServiceHealth> services = serviceHealthRepository.findAll();

        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();

        for (ServiceHealth service : services) {
            long startTime = System.currentTimeMillis();
            try {
                // If service name happens to be a URL, we can actually ping it
                if (service.getServiceName().startsWith("http")) {
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(service.getServiceName()))
                            .GET()
                            .build();
                    java.net.http.HttpResponse<Void> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.discarding());
                    service.setStatus(response.statusCode() < 400 ? HealthStatus.UP : HealthStatus.DOWN);
                    service.setResponseTimeMs(System.currentTimeMillis() - startTime);
                } else {
                    // For non-URL services (like "Cloud SQL Primary"), simulate a basic check or keep previous state
                    // In a full GCP integration, this would call GCP Cloud Monitoring APIs
                    service.setResponseTimeMs(service.getResponseTimeMs() != null ? service.getResponseTimeMs() : 50L);
                }
            } catch (Exception e) {
                log.warn("Health check failed for service: {}", service.getServiceName());
                service.setStatus(HealthStatus.DOWN);
                service.setStatusMessage("Connection failed: " + e.getMessage());
            } finally {
                service.setLastCheckedAt(LocalDateTime.now());
                serviceHealthRepository.save(service);
            }
        }

        log.debug("Health check completed for {} services", services.size());
    }

    /**
     * Entity to DTO conversion - manual mapping, clean aur simple.
     */
    private ServiceHealthDTO toDTO(ServiceHealth entity) {
        return ServiceHealthDTO.builder()
            .id(entity.getId())
            .serviceName(entity.getServiceName())
            .serviceType(entity.getServiceType())
            .status(entity.getStatus())
            .region(entity.getRegion())
            .gcpProjectId(entity.getGcpProjectId())
            .responseTimeMs(entity.getResponseTimeMs())
            .uptimePercentage(entity.getUptimePercentage())
            .lastCheckedAt(entity.getLastCheckedAt())
            .statusMessage(entity.getStatusMessage())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
