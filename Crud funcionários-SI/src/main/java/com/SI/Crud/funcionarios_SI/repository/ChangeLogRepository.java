package com.SI.Crud.funcionarios_SI.repository;

import com.SI.Crud.funcionarios_SI.model.entity.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

    List<ChangeLog> findByEmployeeIdOrderByChangedAtDesc(Long employeeId);

    List<ChangeLog> findAllByOrderByChangedAtDesc();

    List<ChangeLog> findByChangedByOrderByChangedAtDesc(String changedBy);

    // NOVO — filtrar por período de tempo
    List<ChangeLog> findByChangedAtBetweenOrderByChangedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<ChangeLog> findByChangedByAndChangedAtBetweenOrderByChangedAtDesc(String changedBy, LocalDateTime startDate, LocalDateTime endDate);
}
