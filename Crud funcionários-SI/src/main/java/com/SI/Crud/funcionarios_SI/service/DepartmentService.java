package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.exception.ResourceNotFoundException;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento nao encontrado com id: " + id));
    }

    public Department create(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException("Ja existe um departamento cadastrado com este nome.");
        }

        return departmentRepository.save(department);
    }

    public Department update(Long id, Department request) {
        Department department = findById(id);
        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Ja existe outro departamento cadastrado com este nome.");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return departmentRepository.save(department);
    }

    public void delete(Long id) {
        departmentRepository.delete(findById(id));
    }

    @Transactional
    public Department transferEmployeesAndDelete(Long sourceDepartmentId, Long targetDepartmentId, Department newDepartment) {
        Department sourceDepartment = findById(sourceDepartmentId);
        Department targetDepartment = resolveTargetDepartment(sourceDepartmentId, targetDepartmentId, newDepartment);

        List<Employee> employees = employeeRepository.findByDepartmentId(sourceDepartmentId);
        employees.forEach(employee -> employee.setDepartment(targetDepartment));
        employeeRepository.saveAll(employees);
        departmentRepository.delete(sourceDepartment);

        return targetDepartment;
    }

    private Department resolveTargetDepartment(Long sourceDepartmentId, Long targetDepartmentId, Department newDepartment) {
        if (targetDepartmentId != null) {
            if (targetDepartmentId.equals(sourceDepartmentId)) {
                throw new IllegalArgumentException("Escolha um departamento diferente para receber os funcionarios.");
            }
            return findById(targetDepartmentId);
        }

        if (newDepartment == null || newDepartment.getName() == null || newDepartment.getName().isBlank()) {
            throw new IllegalArgumentException("Escolha um departamento existente ou crie um novo para transferir os funcionarios.");
        }

        return create(newDepartment);
    }
}
