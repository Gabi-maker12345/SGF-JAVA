package com.SI.Crud.funcionarios_SI.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_logs")
@Getter
@Setter
public class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false)
    private String field; // ex: "salario", "cargo", "departamento"

    private String action;

    private String entityType;

    private Long entityId;

    private String entityName;

    private String title;

    @Column(length = 2000)
    private String details;

    private String oldValue;

    private String newValue;

    private String changedBy; // email do utilizador que fez a alteração

    private String reason; // motivo da alteração

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
