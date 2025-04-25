package com.example.accesscontrolservice.service;

import com.example.accesscontrolservice.model.AccessLog;
import com.example.accesscontrolservice.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccessLogService {
    @Autowired
    private AccessLogRepository accessLogRepository;

    public List<AccessLog> getAllAccessLogs() {
        return accessLogRepository.findAll();
    }

    public List<AccessLog> getAccessLogsByEmployeeId(String employeeId) {
        return accessLogRepository.findByEmployeeId(employeeId);
    }

    public List<AccessLog> getAccessLogsByAreaId(String areaId) {
        return accessLogRepository.findByAreaId(areaId);
    }

    public List<AccessLog> getAccessLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return accessLogRepository.findByTimestampBetween(start, end);
    }

    public Optional<AccessLog> getAccessLogById(String id) {
        return accessLogRepository.findById(id);
    }

    public AccessLog createAccessLog(AccessLog accessLog) {
        return accessLogRepository.save(accessLog);
    }

    public void deleteAccessLog(String id) {
        accessLogRepository.deleteById(id);
    }
}