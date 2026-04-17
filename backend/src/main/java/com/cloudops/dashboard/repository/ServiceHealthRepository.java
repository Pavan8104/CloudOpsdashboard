package com.cloudops.dashboard.repository;

import com.cloudops.dashboard.model.ServiceHealth;
import com.cloudops.dashboard.model.ServiceHealth.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ServiceHealth repository - service health data ke liye database queries.
 *
 * Dashboard ka main widget yahan se data laata hai.
 * Status summary aur region-wise queries zyada use hote hain.
 */
@Repository
public interface ServiceHealthRepository extends JpaRepository<ServiceHealth, Long> {

    // Service name se dhundho - unique identifier hai practically
    Optional<ServiceHealth> findByServiceName(String serviceName);

    // Ek particular status ke saare services - DOWN services ki list urgent hoti hai
    List<ServiceHealth> findByStatus(HealthStatus status);

    // Region wise services - GCP multi-region setup ke liye
    List<ServiceHealth> findByRegion(String region);

    // Service type filter - Database services, Compute services alag dekhna ho
    List<ServiceHealth> findByServiceType(String serviceType);

    // GCP project ke saari services - multi-project setup ke liye
    List<ServiceHealth> findByGcpProjectId(String gcpProjectId);

    // Saari DOWN ya DEGRADED services - quick alert view ke liye
    List<ServiceHealth> findByStatusIn(List<HealthStatus> statuses);

    // Dashboard summary - kitni UP, kitni DOWN etc. - aggregate query
    @Query("SELECT s.status, COUNT(s) FROM ServiceHealth s GROUP BY s.status")
    List<Object[]> getStatusSummary();

    // Region aur status ke combination se - specific region ki health dekhna ho
    List<ServiceHealth> findByRegionAndStatus(String region, HealthStatus status);

    // Service name contains search - partial match ke liye
    List<ServiceHealth> findByServiceNameContainingIgnoreCase(String name);
}
