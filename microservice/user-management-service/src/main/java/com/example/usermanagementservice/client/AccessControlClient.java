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
    url = "${access-control-service.url:http://localhost:8081}"
)
public interface AccessControlClient {

    @GetMapping("/api/access-logs/employee/{employeeId}")
    List<Object> getAccessLogsByEmployeeId(@PathVariable String employeeId);

    @GetMapping("/api/access-logs/time-range")
    List<Object> getAccessLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);
}

