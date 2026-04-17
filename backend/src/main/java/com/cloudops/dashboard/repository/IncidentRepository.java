package com.cloudops.dashboard.repository;

import com.cloudops.dashboard.model.Incident;
import com.cloudops.dashboard.model.Incident.IncidentStatus;
import com.cloudops.dashboard.model.Incident.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Incident repository - incident tracking ke liye sab database queries yahan hain.
 *
 * On-call engineer ke liye sabse important queries hain -
 * active incidents, unassigned incidents, aur severity filter.
 * MTTR calculations ke liye bhi kuch aggregate queries hain.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Incident number se dhundho - INC-001 type format
    Optional<Incident> findByIncidentNumber(String incidentNumber);

    // Active incidents - OPEN ya IN_PROGRESS wale - on-call dashboard ke liye
    List<Incident> findByStatusIn(List<IncidentStatus> statuses);

    // Specific status ke incidents
    List<Incident> findByStatus(IncidentStatus status);

    // Severity filter - SEV1 incidents urgent hain
    List<Incident> findBySeverity(Severity severity);

    // Specific engineer ke assigned incidents
    List<Incident> findByAssignedToId(Long userId);

    // Unassigned incidents - koi handle nahi kar raha - alert generate karo!
    List<Incident> findByAssignedToIsNullAndStatusNot(IncidentStatus status);

    // Time range mein incidents - reporting ke liye, last 7 din, last 30 din etc.
    List<Incident> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Affected service ke incidents
    List<Incident> findByAffectedServiceId(Long serviceId);

    // Open SEV1 aur SEV2 incidents - critical incidents dashboard widget ke liye
    @Query("SELECT i FROM Incident i WHERE i.severity IN ('SEV1', 'SEV2') AND i.status NOT IN ('RESOLVED', 'CLOSED') ORDER BY i.severity ASC, i.createdAt ASC")
    List<Incident> findActiveCriticalIncidents();

    // MTTR statistics - last 30 din ke resolved incidents ka average time
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (i.resolvedAt - i.startedAt))/60) FROM Incident i WHERE i.status = 'RESOLVED' AND i.resolvedAt >= :since")
    Double getAverageMttrMinutes(@Param("since") LocalDateTime since);

    // Severity aur status ke basis pe count - summary stats ke liye
    @Query("SELECT i.severity, i.status, COUNT(i) FROM Incident i GROUP BY i.severity, i.status")
    List<Object[]> getIncidentSummaryBySeverityAndStatus();

    // Recently resolved incidents - postmortem queue ke liye
    List<Incident> findByStatusOrderByResolvedAtDesc(IncidentStatus status);
}
