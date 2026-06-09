package com.SI.Crud.funcionarios_SI.controller;

import com.SI.Crud.funcionarios_SI.model.entity.User;
import com.SI.Crud.funcionarios_SI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;

    // listar todos os utilizadores
   @GetMapping
public ResponseEntity<List<Map<String, Object>>> findAll() {
    List<Map<String, Object>> users = userRepository.findAll().stream()
            .map(u -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", u.getId());
                map.put("name", u.getName());
                map.put("email", u.getEmail());
                map.put("role", u.getRole());
                return map;
            })
            .toList();
    return ResponseEntity.ok(users);
}

    // alterar role de um utilizador (só ADMIN)
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));

        // validar role permitido
        List<String> allowed = List.of("ROLE_ADMIN", "ROLE_RH", "ROLE_GESTOR", "ROLE_USUARIO");
        if (!allowed.contains(role)) {
            return ResponseEntity.badRequest().build();
        }

        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}