package com.SI.Crud.funcionarios_SI.repository;

import com.SI.Crud.funcionarios_SI.model.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
