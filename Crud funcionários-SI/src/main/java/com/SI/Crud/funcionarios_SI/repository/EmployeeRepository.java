package com.SI.Crud.funcionarios_SI.repository;

import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    long countByDepartmentId(Long departmentId);

    @Query("select e from Employee e join fetch e.department")
    List<Employee> findAllWithDepartment();

    @Query("select coalesce(sum(e.salary), 0) from Employee e")
    BigDecimal sumSalary();

    // NOVO — lixeira
    @Query("select e from Employee e where e.deletedAt is not null")
    List<Employee> findAllInTrash();

    // NOVO — ativos (excluir os que estão na lixeira)
    @Query("select e from Employee e where e.deletedAt is null")
    List<Employee> findAllActive();
}