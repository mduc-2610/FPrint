package com.example.fingerprint_backend.repository;

import com.example.fingerprint_backend.model.Area;
import com.example.fingerprint_backend.model.AreaAccess;
import com.example.fingerprint_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AreaAccessRepository extends JpaRepository<AreaAccess, String> {
    @Query("SELECT new AreaAccess(aa.id, aa.area, aa.timestamp) FROM AreaAccess aa")
    List<AreaAccess> findByEmployeeId(String employeeId);

    boolean existsByEmployeeAndArea(Employee employee, Area area);
    boolean existsByEmployeeIdAndAreaId(String employeeId, String areaId);

}
