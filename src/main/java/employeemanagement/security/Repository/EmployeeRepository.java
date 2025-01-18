package employeemanagement.security.Repository;

import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query("SELECT e FROM Employee e WHERE " +
            "(:department IS NULL OR e.department = :department) AND " +
            "(:status IS NULL OR e.employmentStatus = :status)")
    List<Employee> findByFilters(@Param("department") String department, @Param("status") String status);

    List<Employee> findAll(Specification<Employee> spec, Sort sort);

    @Query("SELECT e FROM Employee e WHERE " +
            "(:fullName IS NULL OR e.fullName LIKE %:fullName%) AND " +
            "(:jobTitle IS NULL OR e.jobTitle LIKE %:jobTitle%) AND " +
            "(:hireDate IS NULL OR e.hireDate = :hireDate) AND " +
            "(:employmentStatus IS NULL OR e.employmentStatus = :employmentStatus) AND " +
            "(:departmentId IS NULL OR e.department.id = :departmentId)")
    List<Employee> findByFilters(String fullName, String jobTitle, LocalDate hireDate,
                                 String employmentStatus, Long departmentId);

    List<Employee> findAllByDepartment(Department managerDepartment);
}
