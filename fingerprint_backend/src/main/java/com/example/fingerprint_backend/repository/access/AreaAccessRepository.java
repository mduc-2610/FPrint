package com.example.fingerprint_backend.repository.access;

import com.example.fingerprint_backend.model.access.Area;
import com.example.fingerprint_backend.model.access.AreaAccess;
import com.example.fingerprint_backend.model.auth.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AreaAccessRepository extends JpaRepository<AreaAccess, String> {
    List<AreaAccess> findByEmployeeId(String employeeId);

    boolean existsByEmployeeAndArea(Employee employee, Area area);

}
