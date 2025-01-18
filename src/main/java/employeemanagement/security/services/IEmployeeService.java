package employeemanagement.security.services;

import employeemanagement.security.DTO.EmployyeDTO;
import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import org.springframework.data.domain.Sort;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

public interface IEmployeeService {
    Employee createEmployee(Employee employee);
    Employee updateEmployee(Long id, Employee updatedEmployee);
    void deleteEmployee(Long id);
    Employee getEmployeeById(Long id);
    List<Employee> getAllEmployees();

//    Employee updateEmployeeManager(Long employeeId, Employee updatedEmployee);


    List<Employee> getEmployeesForManager() throws AccessDeniedException;

    Employee getEmployeeForManager(Long id) throws AccessDeniedException;

    List<Employee> searchEmployees(String name, Long id, String jobTitle, String employmentStatus,
                                   String  department, LocalDate hireDate);
}