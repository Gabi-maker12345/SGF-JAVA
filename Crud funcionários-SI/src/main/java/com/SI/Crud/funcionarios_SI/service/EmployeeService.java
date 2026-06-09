package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import com.SI.Crud.funcionarios_SI.exception.ResourceNotFoundException;
import com.SI.Crud.funcionarios_SI.model.dto.request.EmployeeRequest;
import com.SI.Crud.funcionarios_SI.model.dto.response.EmployeeResponse;
import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ChangeLogService changeLogService;

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAllActive().stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponse findById(Long id) {
        return toResponse(findEmployee(id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ja existe um funcionario cadastrado com este email.");
        }

        Employee employee = new Employee();
        fillEmployee(employee, request);
        Employee savedEmployee = employeeRepository.save(employee);
        changeLogService.recordActivity(
                "Criou funcionario",
                "Funcionario",
                savedEmployee.getId(),
                savedEmployee.getName(),
                null,
                employeeSnapshot(savedEmployee),
                "Funcionario cadastrado e vinculado ao departamento " + savedEmployee.getDepartment().getName() + "."
        );
        return toResponse(savedEmployee);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new IllegalArgumentException("Ja existe outro funcionario cadastrado com este email.");
        }

        String oldValue = employeeSnapshot(employee);
        fillEmployee(employee, request);
        Employee savedEmployee = employeeRepository.save(employee);
        changeLogService.recordActivity(
                "Atualizou funcionario",
                "Funcionario",
                savedEmployee.getId(),
                savedEmployee.getName(),
                oldValue,
                employeeSnapshot(savedEmployee),
                "Dados do funcionario actualizados."
        );
        return toResponse(savedEmployee);
    }

    public void delete(Long id) {
        Employee employee = findEmployee(id);
        employee.setDeletedAt(java.time.LocalDateTime.now());
        employeeRepository.save(employee);
        changeLogService.recordActivity(
                "Enviou funcionario para a lixeira",
                "Funcionario",
                employee.getId(),
                employee.getName(),
                employeeSnapshot(employee),
                "deletedAt=" + employee.getDeletedAt(),
                "Funcionario removido da listagem activa e mantido na lixeira."
        );
    }

    public void restore(Long id) {
        Employee employee = findEmployee(id);
        String oldValue = "deletedAt=" + employee.getDeletedAt();
        employee.setDeletedAt(null);
        employeeRepository.save(employee);
        changeLogService.recordActivity(
                "Restaurou funcionario",
                "Funcionario",
                employee.getId(),
                employee.getName(),
                oldValue,
                employeeSnapshot(employee),
                "Funcionario restaurado para a tabela principal."
        );
    }

    public void deletePermanently(Long id) {
        Employee employee = findEmployee(id);
        if (employee.getDeletedAt() == null) {
            throw new IllegalArgumentException("Envie o funcionario para a lixeira antes de apagar definitivamente.");
        }
        changeLogService.recordActivity(
                "Apagou funcionario definitivamente",
                "Funcionario",
                employee.getId(),
                employee.getName(),
                employeeSnapshot(employee),
                null,
                "Registo removido definitivamente da base de dados."
        );
        employeeRepository.delete(employee);
    }

    public EmployeeResponse updatePhoto(Long id, String photoPath) {
        Employee employee = findEmployee(id);
        String oldValue = employee.getPhotoPath();
        employee.setPhotoPath(photoPath);
        Employee savedEmployee = employeeRepository.save(employee);
        changeLogService.recordActivity(
                "Atualizou fotografia do funcionario",
                "Funcionario",
                savedEmployee.getId(),
                savedEmployee.getName(),
                oldValue,
                photoPath,
                "Fotografia do funcionario actualizada."
        );
        return toResponse(savedEmployee);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionario nao encontrado com id: " + id));
                
    }

    private void fillEmployee(Employee employee, EmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento nao encontrado com id: " + request.getDepartmentId()));

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
                .photoPath(employee.getPhotoPath())
                .build();


    }

    private String employeeSnapshot(Employee employee) {
        Department department = employee.getDepartment();
        return "Nome: " + employee.getName()
                + " | Email: " + employee.getEmail()
                + " | Telefone: " + (employee.getPhone() == null || employee.getPhone().isBlank() ? "Sem telefone" : employee.getPhone())
                + " | Cargo: " + employee.getPosition()
                + " | Salario: " + employee.getSalary()
                + " | Departamento: " + (department != null ? department.getName() : "Sem departamento")
                + " | Estado: " + employee.getStatus();
    }
}
