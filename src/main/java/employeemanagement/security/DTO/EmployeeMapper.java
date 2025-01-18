package employeemanagement.security.DTO;

import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import employeemanagement.security.model.User;

import java.util.Optional;

public class EmployeeMapper {
    public static EmployyeDTO EmptoDto(Employee employee) {
        EmployyeDTO employeDTO = new EmployyeDTO();
        employeDTO.setFullName(employee.getFullName());
        employeDTO.setJobTitle(employee.getJobTitle());
        employeDTO.setHireDate(employee.getHireDate());
        employeDTO.setAddress(employee.getAddress());
        employeDTO.setEmploymentStatus(employee.getEmploymentStatus());
        employeDTO.setContactInfo(employee.getContactInfo());
        employeDTO.setUser(Optional.ofNullable(employee.getUser())
                .map(User::getEmail)
                .orElse("No User"));
        employeDTO.setDepartment(Optional.ofNullable(employee.getDepartment())
                        .map(Department::getName)
                        .orElse("No Department"));

        return employeDTO;
    }
}
