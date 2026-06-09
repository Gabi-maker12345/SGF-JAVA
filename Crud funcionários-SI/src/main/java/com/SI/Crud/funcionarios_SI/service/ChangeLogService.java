package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.dto.response.ChangeLogResponse;
import com.SI.Crud.funcionarios_SI.model.entity.ChangeLog;
import com.SI.Crud.funcionarios_SI.repository.ChangeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeLogService {

    private final ChangeLogRepository changeLogRepository;

    public void record(Long employeeId, String employeeName,
                       String field, String oldValue, String newValue,
                       String changedBy, String reason) {
        ChangeLog log = new ChangeLog();
        log.setEmployeeId(employeeId);
        log.setEmployeeName(employeeName);
        log.setField(field);
        log.setAction(field);
        log.setEntityType("Funcionario");
        log.setEntityId(employeeId);
        log.setEntityName(employeeName);
        log.setTitle(field + " - " + employeeName);
        log.setDetails(reason);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangedBy(changedBy);
        log.setReason(reason);
        log.setChangedAt(LocalDateTime.now());
        changeLogRepository.save(log);
    }

    public void recordActivity(String action, String entityType, Long entityId, String entityName, String oldValue, String newValue, String details) {
        ChangeLog log = new ChangeLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityName(entityName);
        log.setTitle(action + " - " + entityName);
        log.setDetails(details);
        log.setEmployeeId(entityId != null ? entityId : 0L);
        log.setEmployeeName(entityName == null || entityName.isBlank() ? "Sistema" : entityName);
        log.setField(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangedBy(currentUserEmail());
        log.setReason(details);
        log.setChangedAt(LocalDateTime.now());
        changeLogRepository.save(log);
    }

    public List<ChangeLogResponse> findAll() {
        return changeLogRepository.findAllByOrderByChangedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<ChangeLogResponse> findByEmployee(Long employeeId) {
        return changeLogRepository.findByEmployeeIdOrderByChangedAtDesc(employeeId)
                .stream().map(this::toResponse).toList();
    }

    public List<ChangeLogResponse> findForCurrentUser() {
        return changeLogRepository.findByChangedByOrderByChangedAtDesc(currentUserEmail())
                .stream().map(this::toResponse).toList();
    }

    // NOVO — filtrar por período de tempo
    public List<ChangeLogResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return changeLogRepository.findByChangedAtBetweenOrderByChangedAtDesc(startDate, endDate)
                .stream().map(this::toResponse).toList();
    }

    // NOVO — últimos N dias
    public List<ChangeLogResponse> findLastDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        return findByDateRange(startDate, endDate);
    }

    public List<ChangeLogResponse> findCurrentUserLastDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        return changeLogRepository.findByChangedByAndChangedAtBetweenOrderByChangedAtDesc(currentUserEmail(), startDate, endDate)
                .stream().map(this::toResponse).toList();
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "sistema";
        }
        return authentication.getName();
    }

    private ChangeLogResponse toResponse(ChangeLog log) {
        return ChangeLogResponse.builder()
                .id(log.getId())
                .employeeId(log.getEmployeeId())
                .employeeName(log.getEmployeeName())
                .field(log.getField())
                .action(log.getAction() != null ? log.getAction() : log.getField())
                .entityType(log.getEntityType() != null ? log.getEntityType() : "Funcionario")
                .entityId(log.getEntityId() != null ? log.getEntityId() : log.getEmployeeId())
                .entityName(log.getEntityName() != null ? log.getEntityName() : log.getEmployeeName())
                .title(log.getTitle() != null ? log.getTitle() : log.getField() + " - " + log.getEmployeeName())
                .details(log.getDetails() != null ? log.getDetails() : log.getReason())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .changedBy(log.getChangedBy())
                .reason(log.getReason())
                .changedAt(log.getChangedAt())
                .build();
    }
}
