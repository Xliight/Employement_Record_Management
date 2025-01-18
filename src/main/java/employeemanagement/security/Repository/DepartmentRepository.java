package employeemanagement.security.Repository;

import employeemanagement.security.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String departementName);

    Department findByName(String departementName);
}
