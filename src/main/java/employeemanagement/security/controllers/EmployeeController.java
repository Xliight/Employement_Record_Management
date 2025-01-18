package employeemanagement.security.controllers;


import employeemanagement.security.DTO.EmployyeDTO;
import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.EmployeeRepository;
import employeemanagement.security.Repository.UserRepository;
import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import employeemanagement.security.model.User;
import employeemanagement.security.services.IEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort.Order;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') ")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee) {

        try{
            Employee createdEmployee = employeeService.createEmployee(employee);
            return ResponseEntity.ok(createdEmployee);
        }
         catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adding an employee fail: " + ex.getMessage());
        }

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') or (hasAuthority('MANAGER') and hasPermission(#id, 'UPDATE_DEPARTMENT')) ")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,@RequestBody Employee updatedEmployee)  {
        try {
            Employee employee = employeeService.updateEmployee(id, updatedEmployee);
            return ResponseEntity.ok(employee);
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Updating an employee fail: " + ex.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') ")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try{
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();

        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deleting an employee fail: " + ex.getMessage());

        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') or (hasAuthority('MANAGER') and hasPermission(#id, 'VIEW_DEPARTMENT'))")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(employee);
        }
         catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Getting an employee fail: " + ex.getMessage());
    }}



    @GetMapping
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
    @GetMapping("/manager")
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') or hasAuthority('MANAGER')")
    public ResponseEntity<?> getEmployeesForManager() throws AccessDeniedException {
        try{

            List<Employee> employees = employeeService.getEmployeesForManager();
            return ResponseEntity.ok(employees);
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("get employee  fail: " + ex.getMessage());

        }
    }

    @GetMapping("/manager/{id}")
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') or hasAuthority('MANAGER')")
    public ResponseEntity<?> getEmployeeForManager(@PathVariable Long id) throws AccessDeniedException {
        try{

            Employee employees = employeeService.getEmployeeForManager(id);
            return ResponseEntity.ok(employees);
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("get employees  fail: " + ex.getMessage());

        }
    }
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('HR_PERSONNEL') or hasAuthority('ADMINISTRATOR') or hasAuthority('MANAGER') ")
    public List<Employee> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String departement,
            @RequestParam(required = false) LocalDate hireDate) {

        return employeeService.searchEmployees(name, id, jobTitle, employmentStatus, departement, hireDate);
    }
}

