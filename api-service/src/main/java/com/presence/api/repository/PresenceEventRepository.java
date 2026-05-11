package com.presence.api.repository;

import com.presence.api.model.PresenceEvent;
import com.presence.api.model.PresenceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresenceEventRepository extends JpaRepository<PresenceEvent, UUID> {

    /**
     * Most recent event for a device — used as fallback when Redis is cold.
     */
    Optional<PresenceEvent> findTopByDeviceIdOrderByDetectedAtDesc(String deviceId);

    /**
     * Time-range history for one device, newest first.
     */
    @Query("""
        SELECT e FROM PresenceEvent e
        WHERE e.deviceId = :deviceId
          AND e.detectedAt BETWEEN :from AND :to
        ORDER BY e.detectedAt DESC
    """)
    List<PresenceEvent> findHistory(
            @Param("deviceId") String deviceId,
            @Param("from")     long   from,
            @Param("to")       long   to,
            Pageable pageable
    );

    /**
     * Only movement events in a time range — used for stats.
     */
    @Query("""
        SELECT e FROM PresenceEvent e
        WHERE e.deviceId  = :deviceId
          AND e.movement  = true
          AND e.detectedAt BETWEEN :from AND :to
        ORDER BY e.detectedAt DESC
    """)
    List<PresenceEvent> findMovementEvents(
            @Param("deviceId") String deviceId,
            @Param("from")     long   from,
            @Param("to")       long   to
    );

    /**
     * All distinct device IDs that have ever sent data.
     */
    @Query("SELECT DISTINCT e.deviceId FROM PresenceEvent e ORDER BY e.deviceId")
    List<String> findAllDeviceIds();

    /**
     * Count of OCCUPIED events for a device in a time range.
     */
    @Query("""
        SELECT COUNT(e) FROM PresenceEvent e
        WHERE e.deviceId = :deviceId
          AND e.status   = :status
          AND e.detectedAt BETWEEN :from AND :to
    """)
    long countByDeviceIdAndStatusAndDetectedAtBetween(
            @Param("deviceId") String        deviceId,
            @Param("status")   PresenceStatus status,
            @Param("from")     long           from,
            @Param("to")       long           to
    );
}