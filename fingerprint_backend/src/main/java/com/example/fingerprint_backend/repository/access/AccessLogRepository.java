package com.example.fingerprint_backend.repository.access;


import com.example.fingerprint_backend.model.access.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, String> {
    List<AccessLog> findByEmployeeId(String employeeId);
    List<AccessLog> findByAreaId(String areaId);
    List<AccessLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    @Query("SELECT al FROM AccessLog al " +
            "WHERE al.employee.id = :employeeId " +
            "AND (:startDate IS NULL OR al.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR al.timestamp <= :endDate) " +
            "AND (:accessType IS NULL OR al.accessType = :accessType) " +
            "AND (:areaId IS NULL OR al.area.id = :areaId)")
    List<AccessLog> findByEmployeeIdAndTimestampBetween(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("accessType") String accessType,
            @Param("areaId") String areaId
    );
}