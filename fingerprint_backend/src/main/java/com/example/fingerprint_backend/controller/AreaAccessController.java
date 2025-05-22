package com.example.fingerprint_backend.controller;

import com.example.fingerprint_backend.model.Area;
import com.example.fingerprint_backend.model.AreaAccess;
import com.example.fingerprint_backend.model.Employee;
import com.example.fingerprint_backend.repository.AreaRepository;
import com.example.fingerprint_backend.repository.AreaAccessRepository;
import com.example.fingerprint_backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/access")
public class AreaAccessController {
        @Autowired
        private AreaAccessRepository areaAccessRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private AreaRepository areaRepository;

        @GetMapping("/by-employee/{employeeId}")
        public ResponseEntity<List<AreaAccess>> getAccessByEmployee(@PathVariable String employeeId) {
                return ResponseEntity.ok(areaAccessRepository.findByEmployeeId(employeeId));
        }

        @PostMapping("/grant")
        public ResponseEntity<Void> grantAccess(@RequestBody AreaAccess request) {
                boolean accessExists = areaAccessRepository.existsByEmployeeAndArea(request.getEmployee(), request.getArea());
                if (accessExists) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT, "Employee already has access to this area");
                }

                AreaAccess accessPermission = AreaAccess.builder()
                        .employee(request.getEmployee())
                        .area(request.getArea())
                        .timestamp(LocalDateTime.now())
                        .build();

                areaAccessRepository.save(accessPermission);

                return ResponseEntity.ok().build();
        }

        @PostMapping("/grant-all-areas/{employeeId}")
        public ResponseEntity<Void> grantAccessToAllAreas(@PathVariable("employeeId") String employeeId) {
                Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Employee not found with id: " + employeeId));

                List<Area> areas = areaRepository.findAll();
                for (Area area : areas) {
                        boolean accessExists = areaAccessRepository.existsByEmployeeAndArea(employee, area);
                        if (!accessExists) {
                                AreaAccess accessPermission = AreaAccess.builder()
                                        .employee(employee)
                                        .area(area)
                                        .timestamp(LocalDateTime.now())
                                        .build();
                                areaAccessRepository.save(accessPermission);
                        }
                }

                return ResponseEntity.ok().build();
        }


        @DeleteMapping("/revoke/{accessId}")
        public ResponseEntity<Void> revokeAccess(@PathVariable String accessId) {
                if (!areaAccessRepository.existsById(accessId)) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Access permission not found with id: " + accessId);
                }

                areaAccessRepository.deleteById(accessId);

                return ResponseEntity.ok().build();
        }

}
