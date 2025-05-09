package com.example.fingerprint_backend.repository;

import com.example.fingerprint_backend.model.Area;
import com.example.fingerprint_backend.model.AreaAccess;
import com.example.fingerprint_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AreaAccessRepository extends JpaRepository<AreaAccess, String> {
    List<AreaAccess> findByEmployeeId(String employeeId);

    boolean existsByEmployeeAndArea(Employee employee, Area area);

}
