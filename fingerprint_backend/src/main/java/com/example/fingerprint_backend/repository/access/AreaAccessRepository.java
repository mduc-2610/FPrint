package com.example.fingerprint_backend.repository.access;

import com.example.fingerprint_backend.model.access.Area;
import com.example.fingerprint_backend.model.access.AreaAccess;
import com.example.fingerprint_backend.model.auth.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AreaAccessRepository extends JpaRepository<AreaAccess, String> {
    List<AreaAccess> findByEmployee(Employee employee);
    List<AreaAccess> findByEmployeeId(String employeeId);

    List<AreaAccess> findByArea(Area area);
    List<AreaAccess> findByAreaId(String areaId);

    Optional<AreaAccess> findByEmployeeAndArea(Employee employee, Area area);

    boolean existsByEmployeeAndArea(Employee employee, Area area);

    @Transactional
    @Modifying
    @Query("DELETE FROM AreaAccess e WHERE e.employee.id = ?1 AND e.area.id = ?2")
    int deleteByEmployeeIdAndAreaId(String employeeId, String areaId);
}
