package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.exception.ResourceNotFoundException;
import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento nao encontrado com id: " + id));
    }

    public Department create(Department department) {
        return departmentRepository.save(department);
    }

    public Department update(Long id, Department request) {
        Department department = findById(id);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return departmentRepository.save(department);
    }

    public void delete(Long id) {
        departmentRepository.delete(findById(id));
    }
}
