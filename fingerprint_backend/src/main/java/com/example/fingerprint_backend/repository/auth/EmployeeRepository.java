package com.example.fingerprint_backend.repository.auth;

import com.example.fingerprint_backend.model.analytics.EmployeeStatistics;
import com.example.fingerprint_backend.model.auth.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @Query("SELECT new Employee(e.id, e.fullName, e.phoneNumber, e.photo, e.address, e.maxNumberSamples) FROM Employee e")
    List<Employee> findAllEmployees();

    @Query("SELECT new Employee(e.id, e.fullName, e.phoneNumber, e.photo, e.address, e.maxNumberSamples) FROM Employee e WHERE e.id = :id")
    Optional<Employee> findById(String id);

    @Query("SELECT new com.example.fingerprint_backend.model.analytics.EmployeeStatistics(" +
            "e.id, " +
            "e.fullName, " +
            "COUNT(DISTINCT al.id), " +
            "MIN(al.timestamp), " +
            "MAX(al.timestamp)) " +
            "FROM Employee e " +
            "LEFT JOIN e.accessLogs al " +
            "WHERE (:startDate IS NULL OR al.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR al.timestamp <= :endDate) " +
            "GROUP BY e.id, e.fullName")
    List<EmployeeStatistics> getEmployeeStatisticsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}