package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.exception.ResourceNotFoundException;
import com.SI.Crud.funcionarios_SI.model.dto.response.TrashEmployeeResponse;
import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {

    private final EmployeeRepository employeeRepository;

    // listar funcionários na lixeira
    public List<TrashEmployeeResponse> findAllInTrash() {
        return employeeRepository.findAllInTrash()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // mover para lixeira (soft delete)
    public void moveToTrash(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionario nao encontrado: " + id));
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    // restaurar da lixeira
    public void restore(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionario nao encontrado: " + id));
        employee.setDeletedAt(null);
        employeeRepository.save(employee);
    }

    // excluir permanentemente
    public void deletePermanently(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionario nao encontrado: " + id));
        if (employee.getDeletedAt() == null) {
            throw new IllegalStateException("Funcionario nao esta na lixeira.");
        }
        employeeRepository.delete(employee);
    }

    private TrashEmployeeResponse toResponse(Employee employee) {
        Department department = employee.getDepartment();
        return TrashEmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .departmentName(department != null ? department.getName() : null)
                .status(employee.getStatus())
                .deletedAt(employee.getDeletedAt())
                .build();
    }
}