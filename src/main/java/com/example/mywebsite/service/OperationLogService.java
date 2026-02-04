package com.example.mywebsite.service;

import com.example.mywebsite.entity.OperationLog;
import com.example.mywebsite.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Autowired
    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Transactional
    public void logOperation(Long userId, String username, String operationType,
                              String operationDesc, String result, String ipAddress) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setResult(result != null ? result : "success");
        log.setIpAddress(ipAddress);
        log.setCreatedAt(LocalDateTime.now());
        operationLogRepository.save(log);
    }

    public void logOperation(Long userId, String username, String operationType,
                              String operationDesc, String result) {
        logOperation(userId, username, operationType, operationDesc, result, null);
    }

    public void logOperation(Long userId, String username, String operationType,
                              String operationDesc) {
        logOperation(userId, username, operationType, operationDesc, "success", null);
    }

    public List<OperationLog> findWithFilters(String operationType, String startDate,
                                               String endDate, String keyword) {
        return operationLogRepository.findWithFilters(operationType, keyword);
    }

    public List<String> findDistinctOperationTypes() {
        return operationLogRepository.findDistinctOperationTypes();
    }

    public List<OperationLog> findAll() {
        return operationLogRepository.findAll();
    }

    public List<OperationLog> findRecentLogs() {
        List<OperationLog> allLogs = operationLogRepository.findAll();
        if (allLogs.size() > 500) {
            return allLogs.subList(0, 500);
        }
        return allLogs;
    }
}
