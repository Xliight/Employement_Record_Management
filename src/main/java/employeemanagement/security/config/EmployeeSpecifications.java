package employeemanagement.security.config;

import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class EmployeeSpecifications {

    public static Specification<Employee> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name != null && !name.isEmpty()) {
                return criteriaBuilder.like(root.get("fullName"), "%" + name + "%");
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Employee> hasId(Long id) {
        return (root, query, criteriaBuilder) -> {
            if (id != null) {
                return criteriaBuilder.equal(root.get("id"), id);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Employee> hasJobTitle(String jobTitle) {
        return (root, query, criteriaBuilder) -> {
            if (jobTitle != null && !jobTitle.isEmpty()) {
                return criteriaBuilder.like(root.get("jobTitle"), "%" + jobTitle + "%");
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Employee> hasEmploymentStatus(String employmentStatus) {
        return (root, query, criteriaBuilder) -> {
            if (employmentStatus != null && !employmentStatus.isEmpty()) {
                return criteriaBuilder.equal(root.get("employmentStatus"), employmentStatus);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Employee> hasDepartment(String departmentName) {
        return (root, query, criteriaBuilder) -> {
            if (departmentName != null && !departmentName.isEmpty()) {
                return criteriaBuilder.like(root.get("department").get("name"), "%" + departmentName + "%");
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Employee> hiredBefore(LocalDate hireDate) {
        return (root, query, criteriaBuilder) -> {
            if (hireDate != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), hireDate);
            }
            return criteriaBuilder.conjunction();
        };
    }
}