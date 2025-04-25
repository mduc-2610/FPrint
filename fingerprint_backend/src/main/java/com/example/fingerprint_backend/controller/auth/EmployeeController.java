package com.example.fingerprint_backend.controller.auth;

import com.example.fingerprint_backend.model.access.AccessLog;
import com.example.fingerprint_backend.model.analytics.EmployeeStatistics;
import com.example.fingerprint_backend.model.auth.Employee;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintSample;
import com.example.fingerprint_backend.repository.access.AccessLogRepository;
import com.example.fingerprint_backend.repository.auth.EmployeeRepository;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintSampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final FingerprintSampleRepository fingerprintSampleRepository;
    private final EmployeeRepository employeeRepository;
    private final AccessLogRepository accessLogRepository;


    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAllEmployees();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String id, @RequestBody Employee employee) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        employee.setId(id);
        return ResponseEntity.ok(employeeRepository.save(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/fingerprint-samples")
    public ResponseEntity<List<FingerprintSample>> getEmployeeFingerprintSamples(@PathVariable String id) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<FingerprintSample> samples = fingerprintSampleRepository.findByEmployeeId(id);
        return ResponseEntity.ok(samples);
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<EmployeeStatistics>> getEmployeeStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<EmployeeStatistics> statistics = employeeRepository.getEmployeeStatisticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

}
