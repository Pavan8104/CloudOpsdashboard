package com.cloudops.dashboard.repository;

import com.cloudops.dashboard.model.ResourceUsage;
import com.cloudops.dashboard.model.ResourceUsage.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ResourceUsage repository - metrics time series queries ke liye.
 *
 * Ye queries mostly charts ke data ke liye use hote hain.
 * Time range based queries performance ke liye index pe depend karte hain
 * jo model mein define kiye hain.
 * Zyada historical data ho toh pagination add karna padega - abhi simple hai.
 */
@Repository
public interface ResourceUsageRepository extends JpaRepository<ResourceUsage, Long> {

    // Ek service ke saare metrics - time ordered - basic time series
    List<ResourceUsage> findByServiceNameOrderByRecordedAtAsc(String serviceName);

    // Service aur resource type - specific chart ke liye (CPU chart, Memory chart)
    List<ResourceUsage> findByServiceNameAndResourceTypeOrderByRecordedAtAsc(
        String serviceName, ResourceType resourceType);

    // Time range filter - last 1 ghante, last 24 ghante, last 7 din ke liye
    List<ResourceUsage> findByServiceNameAndRecordedAtBetweenOrderByRecordedAtAsc(
        String serviceName, LocalDateTime start, LocalDateTime end);

    // All services ki latest metric - dashboard summary widget ke liye
    @Query("SELECT r FROM ResourceUsage r WHERE r.recordedAt = " +
           "(SELECT MAX(r2.recordedAt) FROM ResourceUsage r2 WHERE r2.serviceName = r.serviceName AND r2.resourceType = r.resourceType)")
    List<ResourceUsage> findLatestMetricsForAllServices();

    // Alert threshold cross kiye hue resources - alert panel ke liye
    @Query("SELECT r FROM ResourceUsage r WHERE r.alertThreshold IS NOT NULL AND " +
           "(CASE WHEN r.maxCapacity IS NOT NULL AND r.maxCapacity > 0 THEN (r.value / r.maxCapacity * 100) ELSE r.value END) >= r.alertThreshold " +
           "AND r.recordedAt >= :since ORDER BY r.recordedAt DESC")
    List<ResourceUsage> findResourcesExceedingThreshold(@Param("since") LocalDateTime since);

    // Region-wise resource usage - GCP multi-region ke liye
    List<ResourceUsage> findByRegionAndResourceTypeOrderByRecordedAtAsc(
        String region, ResourceType resourceType);

    // GCP project ke sare metrics
    List<ResourceUsage> findByGcpProjectIdOrderByRecordedAtDesc(String gcpProjectId);

    // Average resource usage for a service over time range - trend analysis
    @Query("SELECT AVG(r.value) FROM ResourceUsage r WHERE r.serviceName = :serviceName " +
           "AND r.resourceType = :resourceType AND r.recordedAt BETWEEN :start AND :end")
    Double getAverageUsage(@Param("serviceName") String serviceName,
                           @Param("resourceType") ResourceType resourceType,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    // Latest entry for a specific service and resource type
    ResourceUsage findTopByServiceNameAndResourceTypeOrderByRecordedAtDesc(
        String serviceName, ResourceType resourceType);
}
