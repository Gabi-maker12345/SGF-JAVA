package com.SI.Crud.funcionarios_SI.repository;

import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
