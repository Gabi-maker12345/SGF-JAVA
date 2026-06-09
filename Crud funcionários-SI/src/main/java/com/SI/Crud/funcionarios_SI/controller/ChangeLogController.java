package com.SI.Crud.funcionarios_SI.controller;

import com.SI.Crud.funcionarios_SI.model.dto.response.ChangeLogResponse;
import com.SI.Crud.funcionarios_SI.service.ChangeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class ChangeLogController {

    private final ChangeLogService changeLogService;

    // todo o histórico
    @GetMapping
    public ResponseEntity<List<ChangeLogResponse>> findAll() {
        return ResponseEntity.ok(changeLogService.findAll());
    }

    // histórico de um funcionário específico
    @GetMapping("/employees/{id}")
    public ResponseEntity<List<ChangeLogResponse>> findByEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(changeLogService.findByEmployee(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChangeLogResponse>> findCurrentUserHistory(@RequestParam(value = "days", required = false) Integer days) {
        if (days != null && days > 0) {
            return ResponseEntity.ok(changeLogService.findCurrentUserLastDays(days));
        }
        return ResponseEntity.ok(changeLogService.findForCurrentUser());
    }
}
