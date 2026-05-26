package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.exception.ResourceNotFoundException;
import com.SI.Crud.funcionarios_SI.model.dto.request.EmployeeRequest;
import com.SI.Crud.funcionarios_SI.model.dto.response.EmployeeResponse;
import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ChangeLogService changeLogService;

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAllWithDepartmentActive()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponse findById(Long id) {
        return toResponse(findEmployee(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Employee employee = new Employee();
        fillEmployee(employee, request);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);

        String currentUser = getCurrentUserEmail();

        if (request.getSalary() != null &&
            !request.getSalary().equals(employee.getSalary())) {
            changeLogService.record(
                employee.getId(), employee.getName(),
                "salario",
                employee.getSalary() != null ? employee.getSalary().toString() : null,
                request.getSalary().toString(),
                currentUser, request.getReason()
            );
        }

        if (request.getPosition() != null &&
            !request.getPosition().equals(employee.getPosition())) {
            changeLogService.record(
                employee.getId(), employee.getName(),
                "cargo",
                employee.getPosition(),
                request.getPosition(),
                currentUser, request.getReason()
            );
        }

        if (request.getDepartmentId() != null &&
            (employee.getDepartment() == null ||
             !request.getDepartmentId().equals(employee.getDepartment().getId()))) {

            // ← CORRIGIDO: busca o nome do novo departamento em vez de guardar o ID
            String newDeptName = departmentRepository.findById(request.getDepartmentId())
                    .map(Department::getName)
                    .orElse(String.valueOf(request.getDepartmentId()));

            changeLogService.record(
                employee.getId(), employee.getName(),
                "departamento",
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                newDeptName,
                currentUser, request.getReason()
            );
        }

        fillEmployee(employee, request);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
    }

    // ─── métodos privados ───────────────────────────────────────────────────

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "sistema";
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Funcionario nao encontrado com id: " + id));
    }

    private void fillEmployee(Employee employee, EmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Departamento nao encontrado com id: " + request.getDepartmentId()));

        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setDepartment(department);
        employee.setStatus(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ATIVO);
    }

    private EmployeeResponse toResponse(Employee employee) {
        Department department = employee.getDepartment();
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .status(employee.getStatus())
                .build();
    }
}