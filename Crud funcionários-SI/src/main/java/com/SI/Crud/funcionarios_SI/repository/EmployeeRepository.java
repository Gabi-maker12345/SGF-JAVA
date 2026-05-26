package com.SI.Crud.funcionarios_SI.repository;

import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    long countByDepartmentId(Long departmentId);

    @Query("select e from Employee e join fetch e.department")
    List<Employee> findAllWithDepartment();

    @Query("select coalesce(sum(e.salary), 0) from Employee e")
    BigDecimal sumSalary();

    @Query("""
    select e from Employee e
    where (:name is null or lower(e.name) like lower(concat('%', :name, '%')))
    and (:position is null or lower(e.position) like lower(concat('%', :position, '%')))
    and (:departmentId is null or e.department.id = :departmentId)
    and (:status is null or e.status = :status)
    and (:salaryMin is null or e.salary >= :salaryMin)
    and (:salaryMax is null or e.salary <= :salaryMax)
    """)
List<Employee> findWithFilters(
    @Param("name") String name,
    @Param("position") String position,
    @Param("departmentId") Long departmentId,
    @Param("status") EmployeeStatus status,
    @Param("salaryMin") BigDecimal salaryMin,
    @Param("salaryMax") BigDecimal salaryMax
);

@Query("select e.department.name, sum(e.salary), count(e), avg(e.salary) from Employee e group by e.department.name")
List<Object[]> salaryByDepartment();

@Query("select e.position, count(e) from Employee e group by e.position")
List<Object[]> countByPosition();

@Query("select e.status, count(e) from Employee e group by e.status")
List<Object[]> countByStatus();

@Query("select coalesce(sum(e.salary), 0) from Employee e")
BigDecimal sumActiveSalary();

@Query("select coalesce(avg(e.salary), 0) from Employee e")
BigDecimal avgActiveSalary();

@Query("select e from Employee e join fetch e.department")
List<Employee> findAllWithDepartmentActive();

@Query("select e from Employee e left join fetch e.department where e.id = :id")
java.util.Optional<Employee> findByIdWithDepartment(@Param("id") Long id);


}