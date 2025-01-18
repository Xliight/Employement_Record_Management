package employeemanagement.security.services.impl;

import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.DepartmentRepository;
import employeemanagement.security.Repository.EmployeeRepository;
import employeemanagement.security.Repository.UserRepository;
import employeemanagement.security.config.ApplicationAuditAware;
import employeemanagement.security.config.EmployeeSpecifications;
import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import employeemanagement.security.model.User;
import employeemanagement.security.services.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditLogger auditLogger;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ApplicationAuditAware auditAware;
    @Override
    public Employee createEmployee(Employee employee) {
        Optional<Department> department = Optional.ofNullable(departmentRepository.findById(employee.getDepartment().getId()).orElseThrow(() -> new ResourceNotFoundException("Employee not found")));;
            employee.setDepartment(department.get());
            Employee savedEmployee = employeeRepository.save(employee);
            String performedBy = auditLogger.resolvePerformedBy();
            auditLogger.logAudit("CREATE", performedBy, "Created employee with ID: " + savedEmployee.getId());
            return savedEmployee;


    }

    @Override
    public Employee updateEmployee(Long id, Employee updatedEmployee)    {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!isAuthorizedToUpdateEmployee(id)) {
            throw  new ResourceNotFoundException("Unauthorized to update this employee");
        }

        String role = getCurrentUserRole();

        if ("MANAGER".equals(role)) {
            existingEmployee.setJobTitle(updatedEmployee.getJobTitle());
            existingEmployee.setEmploymentStatus(updatedEmployee.getEmploymentStatus());

        } else if ("ADMINISTRATOR".equals(role) || "HR_PERSONNEL".equals(role)) {
            Optional<Department> department =departmentRepository.findById(updatedEmployee.getDepartment().getId());
            existingEmployee.setFullName(updatedEmployee.getFullName());
            existingEmployee.setJobTitle(updatedEmployee.getJobTitle());
            existingEmployee.setDepartment(department.get());
            existingEmployee.setHireDate(updatedEmployee.getHireDate());
            existingEmployee.setEmploymentStatus(updatedEmployee.getEmploymentStatus());
            existingEmployee.setContactInfo(updatedEmployee.getContactInfo());
            existingEmployee.setAddress(updatedEmployee.getAddress());
        }

        employeeRepository.save(existingEmployee);
        String performedBy = auditLogger.resolvePerformedBy();

        auditLogger.logAudit("UPDATE", performedBy, "Updated employee with ID: " + id);
        return existingEmployee;
    }

    public boolean isAuthorizedToUpdateEmployee(Long employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userRepository.findByEmail(email);
        Optional<Employee> manager = employeeRepository.findById(Long.valueOf(user.get().getId()));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if ("HR_PERSONNEL".equals(user.get().getRole().getName()) || "ADMINISTRATOR".equals(user.get().getRole().getName())) {
            return true;
        }

        return manager.isPresent() && manager.get().getDepartment().getId().equals(employee.getDepartment().getId());
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> user = userRepository.findByEmail(email);
        return user.map(value -> value.getRole().getName()).orElse("UNKNOWN");
    }

    @Override
    public void deleteEmployee(Long id) {
        String performedBy = auditLogger.resolvePerformedBy();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);

        auditLogger.logAudit("DELETE", performedBy, "Deleted employee with ID: " + id);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Override
    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }
    @Override
    public List<Employee> getEmployeesForManager() throws AccessDeniedException {
        // Get the current logged-in user's details (manager in this case)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        List<Employee> employees;
        if (userOptional.get().getRole().getName().equals("ADMINISTRATOR") || userOptional.get().getRole().getName().equals("HR_PERSONNEL")) {
             employees = employeeRepository.findAll();

        }else {
            User user = userOptional.get();
            Department managerDepartment = user.getEmployee().getDepartment();
            if(managerDepartment == null) {
                throw new ResourceNotFoundException("You have no Department ");
            }
             employees = employeeRepository.findAllByDepartment(managerDepartment);
        }


        return employees;
    }
    @Override
    public Employee getEmployeeForManager(Long id) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(email);
        Optional<Employee> employees;
        if (userOptional.get().getRole().getName().equals("ADMINISTRATOR") || userOptional.get().getRole().getName().equals("HR_PERSONNEL")) {
            employees = Optional.ofNullable(employeeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found")));

        }else {
            User user = userOptional.get();
            Department managerDepartment = user.getEmployee().getDepartment();
            if(managerDepartment == null) {
                throw new ResourceNotFoundException("You have no Department ");
            }
            employees = Optional.ofNullable(employeeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found")));
            if (employees.get().getDepartment().getName().equals(managerDepartment)){
                return employees.get();
            }
        }


        return employees.get();
    }



    @Override
    public List<Employee> searchEmployees(String name, Long id, String jobTitle, String employmentStatus,
                                          String department, LocalDate hireDate) {
        String role = getCurrentUserRole();
        Specification<Employee> spec = Specification.where(EmployeeSpecifications.hasName(name))
                .and(EmployeeSpecifications.hasId(id))
                .and(EmployeeSpecifications.hasJobTitle(jobTitle))
                .and(EmployeeSpecifications.hasEmploymentStatus(employmentStatus))
                .and(EmployeeSpecifications.hasDepartment(department))
                .and(EmployeeSpecifications.hiredBefore(hireDate));

        if ("MANAGER".equals(role)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            System.out.println(email);
            Optional<User> user = userRepository.findByEmail(email);
            System.out.println(user.get().getEmail());
            Optional<Employee> employee=employeeRepository.findById(Long.valueOf(user.get().getEmployee().getId()));
            Department managerDepartment = employee.get().getDepartment();

            if (managerDepartment != null) {
                spec = spec.and(EmployeeSpecifications.hasDepartment(managerDepartment.getName()));
                return employeeRepository.findAll(spec);
            } else {
                throw new ResourceNotFoundException("You have no departement");
            }
        }

        return employeeRepository.findAll(spec);
    }















}
