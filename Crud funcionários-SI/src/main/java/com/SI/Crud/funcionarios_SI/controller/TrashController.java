package com.SI.Crud.funcionarios_SI.controller;

import com.SI.Crud.funcionarios_SI.model.dto.response.TrashEmployeeResponse;
import com.SI.Crud.funcionarios_SI.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    // listar tudo na lixeira
    @GetMapping
    public ResponseEntity<List<TrashEmployeeResponse>> findAllInTrash() {
        return ResponseEntity.ok(trashService.findAllInTrash());
    }

    // mover para lixeira
    @PostMapping("/employees/{id}")
    public ResponseEntity<Void> moveToTrash(@PathVariable Long id) {
        trashService.moveToTrash(id);
        return ResponseEntity.noContent().build();
    }

    // restaurar
    @PostMapping("/employees/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        trashService.restore(id);
        return ResponseEntity.noContent().build();
    }

    // excluir permanentemente
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deletePermanently(@PathVariable Long id) {
        trashService.deletePermanently(id);
        return ResponseEntity.noContent().build();
    }
}