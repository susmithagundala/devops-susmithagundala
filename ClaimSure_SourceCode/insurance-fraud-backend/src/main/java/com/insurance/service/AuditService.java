package com.insurance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.entity.AppUser;
import com.insurance.entity.AuditLog;
import com.insurance.repository.AuditLogRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(
            AppUser actor,
            String action,
            String entityType,
            Long entityId,
            Object details,
            String ipAddress
    ) {
        AuditLog log = new AuditLog();

        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setIpAddress(ipAddress);

        JsonNode jsonDetails = details == null
                ? objectMapper.createObjectNode()
                : objectMapper.valueToTree(details);

        log.setDetails(jsonDetails);

        auditLogRepository.save(log);
    }
}
