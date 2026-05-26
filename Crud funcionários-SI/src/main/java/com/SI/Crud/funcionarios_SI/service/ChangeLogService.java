package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.dto.response.ChangeLogResponse;
import com.SI.Crud.funcionarios_SI.model.entity.ChangeLog;
import com.SI.Crud.funcionarios_SI.repository.ChangeLogRepository;
import lombok.RequiredArgsConstructor;
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
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangedBy(changedBy);
        log.setReason(reason);
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

    private ChangeLogResponse toResponse(ChangeLog log) {
        return ChangeLogResponse.builder()
                .id(log.getId())
                .employeeId(log.getEmployeeId())
                .employeeName(log.getEmployeeName())
                .field(log.getField())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .changedBy(log.getChangedBy())
                .reason(log.getReason())
                .changedAt(log.getChangedAt())
                .build();
    }
}