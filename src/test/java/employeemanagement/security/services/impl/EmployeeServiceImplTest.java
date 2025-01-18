package employeemanagement.security.services.impl;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.DepartmentRepository;
import employeemanagement.security.Repository.EmployeeRepository;
import employeemanagement.security.Repository.UserRepository;
import employeemanagement.security.model.Department;
import employeemanagement.security.model.Employee;
import employeemanagement.security.model.Role;
import employeemanagement.security.model.User;

public class EmployeeServiceImplTest {
    @MockBean
    private Authentication authentication;
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee mockEmployee;



    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
         authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);




    }


    @Test
    public void testCreateEmployeeSuccess() {
     
       Department mockDepartmentt = new Department();
        mockDepartmentt.setId(1L);  
        mockDepartmentt.setName("Sales");  

        
        mockEmployee = new Employee();
        mockEmployee.setFullName("John Doe");
        mockEmployee.setJobTitle("Developer");
        mockEmployee.setDepartment(mockDepartmentt); 

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(mockDepartmentt));
        when(employeeRepository.save(mockEmployee)).thenReturn(mockEmployee);

        Employee createdEmployee = employeeService.createEmployee(mockEmployee);
        verify(employeeRepository, times(1)).save(mockEmployee);
        assertEquals("John Doe", createdEmployee.getFullName());
    }

    @Test
    public void testCreateEmployeeDepartmentNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        Employee mockEmployee = new Employee();
        mockEmployee.setFullName("John Doe");
        mockEmployee.setJobTitle("Developer");
        mockEmployee.setDepartment(null);
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(mockEmployee));
    }
    
    @Test
    public void testDeleteEmployeeSuccessfully() {
        Department mockDepartmentt = new Department();
        mockDepartmentt.setId(1L);
        mockDepartmentt.setName("Sales");

        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setFullName("John Doe");
        mockEmployee.setJobTitle("Developer");
        mockEmployee.setDepartment(mockDepartmentt);
       
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(mockEmployee);
    }
    @Test
    public void testDeleteEmployeeNotFound() {
       
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

       
        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.deleteEmployee(1L));
    }

    @Test
    public void testGetEmployeeByIdSuccess() {
        Department mockDepartmentt = new Department();
        mockDepartmentt.setId(1L);
        mockDepartmentt.setName("Sales");
        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setFullName("John Doe");
        mockEmployee.setJobTitle("Developer");
        mockEmployee.setDepartment(mockDepartmentt);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        Employee foundEmployee = employeeService.getEmployeeById(1L);

      
        assertNotNull(foundEmployee);
        assertEquals(1L, foundEmployee.getId());
        assertEquals("John Doe", foundEmployee.getFullName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

      
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(1L));
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAllEmployeesSuccess() {
        Department mockDepartmentt = new Department();
        mockDepartmentt.setId(1L);
        mockDepartmentt.setName("Sales");
        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setFullName("John Doe");
        mockEmployee.setJobTitle("Developer");
        mockEmployee.setDepartment(mockDepartmentt);
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFullName("Jane Doe");
        employee.setJobTitle("Developer");
        employee.setDepartment(mockDepartmentt);

        
        List<Employee> employees = Arrays.asList(
                mockEmployee,
                employee
        );

        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> foundEmployees = employeeService.getAllEmployees();

        assertNotNull(foundEmployees);
        assertEquals(2, foundEmployees.size());
        assertEquals("John Doe", foundEmployees.get(0).getFullName());
        assertEquals("Jane Doe", foundEmployees.get(1).getFullName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllEmployeesEmpty() {
       
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

       
        List<Employee> foundEmployees = employeeService.getAllEmployees();

       
        assertNotNull(foundEmployees);
        assertTrue(foundEmployees.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    public void testIsAuthorizedToUpdateEmployeeAsHRPersonnel() {
     
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@company.com");
        Role role = new Role();
        role.setName("HR_PERSONNEL");
        mockUser.setRole(role);

       
        Employee mockEmployee = new Employee();
        mockEmployee.setId(2L);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(mockEmployee));

        boolean isAuthorized = employeeService.isAuthorizedToUpdateEmployee(2L);

        assertTrue(isAuthorized); 
        verify(userRepository, times(1)).findByEmail("user@company.com");
        verify(employeeRepository, times(1)).findById(2L);
    }

    @Test
    public void testIsAuthorizedToUpdateEmployeeAsAdministrator() {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@company.com");
        Role role = new Role();
        role.setName("ADMINISTRATOR");
        mockUser.setRole(role);

        Employee mockEmployee = new Employee();
        mockEmployee.setId(3L);
        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(mockEmployee));

        boolean isAuthorized = employeeService.isAuthorizedToUpdateEmployee(3L);

        verify(userRepository, times(1)).findByEmail("user@company.com");
        verify(employeeRepository, times(1)).findById(3L);
    }

    @Test
    public void testIsAuthorizedToUpdateEmployeeAsManagerSameDepartment() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@company.com");
        Role role = new Role();
        role.setName("MANAGER");
        mockUser.setRole(role);
        Department mockDepartment = new Department();
        mockDepartment.setId(101L);
        Employee mockManager = new Employee();
        mockManager.setId(1L);
        mockManager.setDepartment(mockDepartment);

        Employee mockEmployee = new Employee();
        mockEmployee.setId(4L);
        mockEmployee.setDepartment(mockDepartment);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockManager));
        when(employeeRepository.findById(4L)).thenReturn(Optional.of(mockEmployee));

        boolean isAuthorized = employeeService.isAuthorizedToUpdateEmployee(4L);

        assertTrue(isAuthorized);
        verify(userRepository, times(1)).findByEmail("user@company.com");
        verify(employeeRepository, times(2)).findById(anyLong());
    }

    @Test
    public void testIsAuthorizedToUpdateEmployeeAsManagerDifferentDepartment() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@company.com");
        Role role = new Role();
        role.setName("MANAGER");
        mockUser.setRole(role);

        
        Department department1 = new Department();
        department1.setId(101L);

        Department department2 = new Department();
        department2.setId(102L);

        Employee mockManager = new Employee();
        mockManager.setId(1L);
        mockManager.setDepartment(department1);

        Employee mockEmployee = new Employee();
        mockEmployee.setId(5L);
        mockEmployee.setDepartment(department2);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockManager));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(mockEmployee));

        boolean isAuthorized = employeeService.isAuthorizedToUpdateEmployee(5L);

        assertFalse(isAuthorized); 
        verify(userRepository, times(1)).findByEmail("user@company.com");
        verify(employeeRepository, times(2)).findById(anyLong());
    }

    @Test
    public void testIsAuthorizedToUpdateEmployeeEmployeeNotFound() {
        
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@company.com");
        Role role = new Role();
        role.setName("MANAGER");
        mockUser.setRole(role);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.isAuthorizedToUpdateEmployee(99L));

    }

    @Test
    public void testGetCurrentUserRoleWhenUserHasRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
       
        User mockUser = new User();
        mockUser.setEmail("user@company.com");
        Role role = new Role();
        role.setName("HR_PERSONNEL");
        mockUser.setRole(role);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));

        String roleName = employeeService.getCurrentUserRole();

        assertEquals("HR_PERSONNEL", roleName);
        verify(userRepository, times(1)).findByEmail("user@company.com");
    }

    @Test
    public void testGetCurrentUserRoleWhenUserDoesNotExist() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.empty());

        String roleName = employeeService.getCurrentUserRole();

        assertEquals("UNKNOWN", roleName);
        verify(userRepository, times(1)).findByEmail("user@company.com");
    }

    @Test
    public void testGetCurrentUserRoleWhenRoleIsNull() {
       
        User mockUser = new User();
        mockUser.setEmail("user@company.com");
        mockUser.setRole(null);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(mockUser));

        assertThrows(NullPointerException.class, () -> employeeService.getCurrentUserRole());
    }

    @Test
    public void testGetEmployeesForAdmin() throws AccessDeniedException {
           User adminUser = new User();
        Role adminRole = new Role();
        adminRole.setName("ADMINISTRATOR");
        adminUser.setRole(adminRole);
        adminUser.setEmail("user@company.com");

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(adminUser));
        List<Employee> mockEmployees = List.of(new Employee(), new Employee());
        when(employeeRepository.findAll()).thenReturn(mockEmployees);

        List<Employee> employees = employeeService.getEmployeesForManager();

        assertEquals(2, employees.size());
        verify(employeeRepository, times(1)).findAll();
        verify(employeeRepository, never()).findAllByDepartment(any());
    }

    @Test
    public void testGetEmployeesForHrPersonnel() throws AccessDeniedException {
        
        User hrUser = new User();
        Role hrRole = new Role();
        hrRole.setName("HR_PERSONNEL");
        hrUser.setRole(hrRole);
        hrUser.setEmail("user@company.com");

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(hrUser));
        List<Employee> mockEmployees = List.of(new Employee());
        when(employeeRepository.findAll()).thenReturn(mockEmployees);

        List<Employee> employees = employeeService.getEmployeesForManager();

        assertEquals(1, employees.size());
        verify(employeeRepository, times(1)).findAll();
        verify(employeeRepository, never()).findAllByDepartment(any());
    }


    @Test
    public void testGetEmployeesForManagerWithDepartment() throws AccessDeniedException {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);        User managerUser = new User();
        Role managerRole = new Role();
        managerRole.setName("MANAGER");
        managerUser.setRole(managerRole);
        managerUser.setEmail("user@company.com");

        Department mockDepartment = new Department();
        managerUser.setEmployee(new Employee());
        managerUser.getEmployee().setDepartment(mockDepartment);

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(managerUser));
        List<Employee> mockEmployees = List.of(new Employee(), new Employee());
        when(employeeRepository.findAllByDepartment(mockDepartment)).thenReturn(mockEmployees);

        List<Employee> employees = employeeService.getEmployeesForManager();

        assertEquals(2, employees.size());
        verify(employeeRepository, times(1)).findAllByDepartment(mockDepartment);
        verify(employeeRepository, never()).findAll();
    }

    @Test
    public void testGetEmployeesForManagerWithoutDepartment() {
        
        User managerUser = new User();
        Role managerRole = new Role();
        managerRole.setName("MANAGER");
        managerUser.setRole(managerRole);
        managerUser.setEmail("user@company.com");
        managerUser.setEmployee(new Employee());

        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.of(managerUser));
        managerUser.getEmployee().setDepartment(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeesForManager());

        assertEquals("You have no Department ", exception.getMessage());
    }

    @Test
    public void testGetEmployeesForManagerUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("user@company.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(NoSuchElementException.class, () -> employeeService.getEmployeesForManager());
        assertEquals("No value present", exception.getMessage());
        verify(employeeRepository, never()).findAll();
        verify(employeeRepository, never()).findAllByDepartment(any());
    }

    @Test
    public void testSearchEmployeesWithAllFilters() {
        
        String name = "John Doe";
        Long id = 1L;
        String jobTitle = "Developer";
        String employmentStatus = "ACTIVE";
        String departmentName = "Engineering"; 
        LocalDate hireDate = LocalDate.of(2022, 1, 1);

        
        Department department = new Department();
        department.setId(2L);
        department.setName("Engineering"); 

        Employee mockEmployee = new Employee();
        mockEmployee.setFullName("John Doe");
        mockEmployee.setJobTitle("Developer");
        mockEmployee.setDepartment(department);

        Employee mockEmployee2 = new Employee();
        mockEmployee2.setFullName("John Doe");
        mockEmployee2.setJobTitle("Developer");
        mockEmployee2.setDepartment(department);

        
        List<Employee> mockEmployees = Arrays.asList(mockEmployee, mockEmployee2);

        
        when(employeeRepository.findAll(any(Specification.class))).thenReturn(mockEmployees);

        
        List<Employee> employees = employeeService.searchEmployees(name, id, jobTitle, employmentStatus, departmentName, hireDate);

        assertEquals(2, employees.size());
        verify(employeeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testSearchEmployeesWithPartialFilters() {
        
        String name = null; 
        Long id = null; 
        String jobTitle = "Manager"; 
        String employmentStatus = null; 
        String departmentName = null; 
        LocalDate hireDate = null; 

        Employee mockEmployee2 = new Employee();
        mockEmployee2.setFullName("Alice Smith");
        mockEmployee2.setJobTitle("Developer");
        mockEmployee2.setDepartment(null); 

        
        List<Employee> mockEmployees = Collections.singletonList(mockEmployee2);

        when(employeeRepository.findAll(any(Specification.class))).thenReturn(mockEmployees);

        
        List<Employee> employees = employeeService.searchEmployees(name, id, jobTitle, employmentStatus, departmentName, hireDate);

       
        assertEquals(1, employees.size());
        assertEquals("Alice Smith", employees.get(0).getFullName());
        verify(employeeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testSearchEmployeesWithNoResults() {
        
        String name = "Nonexistent";
        Long id = 999L;
        String jobTitle = "Nonexistent Job";
        String employmentStatus = "INACTIVE";
        String departmentName = "Nonexistent Department"; 
        LocalDate hireDate = LocalDate.of(1900, 1, 1);

        when(employeeRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        
        List<Employee> employees = employeeService.searchEmployees(name, id, jobTitle, employmentStatus, departmentName, hireDate);

        
        assertTrue(employees.isEmpty());
        verify(employeeRepository, times(1)).findAll(any(Specification.class));
    }




}
