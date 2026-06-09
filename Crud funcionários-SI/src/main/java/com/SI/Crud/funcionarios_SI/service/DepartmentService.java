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
    private final ChangeLogService changeLogService;

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

        Department savedDepartment = departmentRepository.save(department);
        changeLogService.recordActivity(
                "Criou departamento",
                "Departamento",
                savedDepartment.getId(),
                savedDepartment.getName(),
                null,
                departmentSnapshot(savedDepartment),
                "Departamento criado no sistema."
        );
        return savedDepartment;
    }

    public Department update(Long id, Department request) {
        Department department = findById(id);
        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Ja existe outro departamento cadastrado com este nome.");
        }

        String oldValue = departmentSnapshot(department);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        Department savedDepartment = departmentRepository.save(department);
        changeLogService.recordActivity(
                "Atualizou departamento",
                "Departamento",
                savedDepartment.getId(),
                savedDepartment.getName(),
                oldValue,
                departmentSnapshot(savedDepartment),
                "Dados do departamento actualizados."
        );
        return savedDepartment;
    }

    public void delete(Long id) {
        Department department = findById(id);
        changeLogService.recordActivity(
                "Apagou departamento",
                "Departamento",
                department.getId(),
                department.getName(),
                departmentSnapshot(department),
                null,
                "Departamento removido do sistema."
        );
        departmentRepository.delete(department);
    }

    @Transactional
    public Department transferEmployeesAndDelete(Long sourceDepartmentId, Long targetDepartmentId, Department newDepartment) {
        Department sourceDepartment = findById(sourceDepartmentId);
        Department targetDepartment = resolveTargetDepartment(sourceDepartmentId, targetDepartmentId, newDepartment);

        List<Employee> employees = employeeRepository.findByDepartmentId(sourceDepartmentId);
        employees.forEach(employee -> employee.setDepartment(targetDepartment));
        employeeRepository.saveAll(employees);
        changeLogService.recordActivity(
                "Transferiu funcionarios e apagou departamento",
                "Departamento",
                sourceDepartment.getId(),
                sourceDepartment.getName(),
                "Departamento origem: " + sourceDepartment.getName(),
                "Departamento destino: " + targetDepartment.getName(),
                employees.size() + " funcionario(s) transferido(s) antes da remocao do departamento."
        );
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

    private String departmentSnapshot(Department department) {
        return "Nome: " + department.getName()
                + " | Descricao: " + (department.getDescription() == null || department.getDescription().isBlank() ? "Sem descricao" : department.getDescription());
    }
}
