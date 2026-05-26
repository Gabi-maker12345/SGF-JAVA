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

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
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
        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new IllegalArgumentException("Ja existe outro funcionario cadastrado com este email.");
        }

        fillEmployee(employee, request);
        return toResponse(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
    }

    public EmployeeResponse updatePhoto(Long id, String photoPath) {
        Employee employee = findEmployee(id);
        employee.setPhotoPath(photoPath);
        return toResponse(employeeRepository.save(employee));
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
}
