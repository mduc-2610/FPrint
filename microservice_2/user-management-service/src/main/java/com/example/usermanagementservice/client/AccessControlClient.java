package com.example.usermanagementservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(
    name = "access-control-service",
    url = "${access-control-service.url}"
)
public interface AccessControlClient {

    @GetMapping("/access-log/employee/{employeeId}")
    List<Object> getAccessLogsByEmployeeId(@PathVariable String employeeId);

    @GetMapping("/access-log/time-range")
    List<Object> getAccessLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);
            
    @GetMapping("/access-log/filtered")
    List<Object> getFilteredAccessLogs(
            @RequestParam String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String accessType,
            @RequestParam(required = false) String areaId);
}