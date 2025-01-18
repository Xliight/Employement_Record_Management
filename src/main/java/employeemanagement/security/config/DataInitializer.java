package employeemanagement.security.config;

import employeemanagement.security.Repository.*;
import employeemanagement.security.model.*;
import employeemanagement.security.payload.RegisterRequest;
import employeemanagement.security.services.impl.AdminService;
import employeemanagement.security.services.impl.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AdminService adminService;

    @Bean
    public CommandLineRunner initializeData(AuthenticationService authenticationService) {
        return args -> {
            // Create departments
            Department hr = new Department(1L, "Human Resources", "1");
            Department engineering = new Department(2L, "Engineering", "2");
            Department marketing = new Department(3L, "Marketing", "3");
            departmentRepository.saveAll(List.of(hr, engineering, marketing));

            // Create employees
            Employee john = new Employee(1L, "John Doe", "HR Specialist", LocalDate.of(2020, 1, 1),
                    "Full-Time", "123-456-7890", "123 Main St", hr);
            Employee jane = new Employee(2L, "Jane Smith", "Recruiter", LocalDate.of(2021, 5, 15),
                    "Part-Time", "987-654-3210", "456 Oak St", hr);
            Employee alice = new Employee(3L, "Alice Brown", "Software Engineer", LocalDate.of(2019, 11, 30),
                    "Full-Time", "555-555-5555", "789 Pine St", engineering);
            Employee bob = new Employee(4L, "Bob White", "QA Engineer", LocalDate.of(2022, 7, 10),
                    "Part-Time", "666-666-6666", "101 Maple Ave", engineering);
            Employee charlie = new Employee(5L, "Charlie Black", "Marketing Specialist", LocalDate.of(2023, 3, 20),
                    "Full-Time", "777-777-7777", "202 Birch Rd", marketing);
            Employee diana = new Employee(6L, "Diana Green", "Content Creator", LocalDate.of(2021, 9, 12),
                    "Part-Time", "888-888-8888", "303 Elm St", marketing);
            employeeRepository.saveAll(List.of(john, jane, alice, bob, charlie, diana));

            Role adminRole = new Role();
            Role hrRole = new Role();
            Role managerRole = new Role();

            if (!roleRepository.existsByName("ADMINISTRATOR")) {
                adminRole.setName("ADMINISTRATOR");
                roleRepository.save(adminRole);
            }
            if (!roleRepository.existsByName("HR_PERSONNEL")) {
                hrRole.setName("HR_PERSONNEL");
                roleRepository.save(hrRole);
            }
            if (!roleRepository.existsByName("MANAGER")) {
                managerRole.setName("MANAGER");
                roleRepository.save(managerRole);
            }

            // Create permissions if not exist
            if (!permissionRepository.existsByName("EMPLOYEE_READ")) {
                Permission employeeReadPermission = new Permission("EMPLOYEE_READ");
                permissionRepository.save(employeeReadPermission);
            }
            if (!permissionRepository.existsByName("EMPLOYEE_CREATE")) {
                Permission employeeCreatePermission = new Permission("EMPLOYEE_CREATE");
                permissionRepository.save(employeeCreatePermission);
            }
            if (!permissionRepository.existsByName("EMPLOYEE_UPDATE")) {
                Permission employeeUpdatePermission = new Permission("EMPLOYEE_UPDATE");
                permissionRepository.save(employeeUpdatePermission);
            }
            if (!permissionRepository.existsByName("EMPLOYEE_DELETE")) {
                Permission employeeDeletePermission = new Permission("EMPLOYEE_DELETE");
                permissionRepository.save(employeeDeletePermission);
            }

            // Assign permissions to roles
            adminService.assignPermissionsToRolee("ADMINISTRATOR", Set.of("EMPLOYEE_READ", "EMPLOYEE_CREATE", "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE"));
            adminService.assignPermissionsToRolee("MANAGER", Set.of("EMPLOYEE_READ", "EMPLOYEE_CREATE"));
            adminService.assignPermissionsToRolee("HR_PERSONNEL", Set.of("EMPLOYEE_READ", "EMPLOYEE_CREATE", "EMPLOYEE_UPDATE", "EMPLOYEE_DELETE"));



            RegisterRequest adminUser = new RegisterRequest();
            adminUser.setFirstname("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("xlight06");
            adminUser.setRole(adminRole);
            authenticationService.register(adminUser);

            RegisterRequest hrUser = new RegisterRequest();
            hrUser.setFirstname("hrperson");
            hrUser.setEmail("hr@example.com");
            hrUser.setPassword("xlight06");
            hrUser.setRole(hrRole);
            authenticationService.register(hrUser);

            RegisterRequest managerUser = new RegisterRequest();
            managerUser.setFirstname("manager");
            managerUser.setEmail("manager@example.com");
            managerUser.setPassword("xlight06");
            managerUser.setRole(managerRole);
            authenticationService.register(managerUser);
        };
    }
}
