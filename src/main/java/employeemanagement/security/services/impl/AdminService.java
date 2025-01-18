package employeemanagement.security.services.impl;

import employeemanagement.security.DTO.UserDTO;
import employeemanagement.security.DTO.UserMapper;
import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.*;
import employeemanagement.security.model.*;
import employeemanagement.security.services.IAdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final RoleRepository roleRepository;
    private final AuditLogger auditLogger;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void assignPermissionsToRolee(String roleName, Set<String> permissionNames) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Explicitly load permissions into the session before adding them to the role.
        Set<Permission> permissions = new HashSet<>();
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException("Permission not found"));
            permissions.add(permission);
        }
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @Override
    public Permission addPermission(String permissionName) {
        if (permissionRepository.existsByName(permissionName)) {
            throw new ResourceNotFoundException("Permission already exists");
        }
        Permission permission = new Permission(permissionName);

        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("CREATE", performedBy, "add Permission: " + permissionName);
        return permissionRepository.save(permission);

    }
    @Override
    public Role addRole(String roleName) {
        if (roleRepository.existsByName(roleName)) {
            throw new ResourceNotFoundException("Role already exists");
        }
        Role role = new Role(roleName);
        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("CREATE", performedBy, "add role: " + roleName);
        return roleRepository.save(role);
    }
    @Override
    public void assignPermmission(String roleName, String permissionName) {
        if (!roleRepository.existsByName(roleName)) {
            throw new ResourceNotFoundException("Role does not  exists");
        }
        if (!permissionRepository.existsByName(permissionName)) {
            throw new ResourceNotFoundException("Permission does not  exists");
        }

        Optional<Role> rolee=roleRepository.findByName(roleName);
        Optional<Permission> permissione=permissionRepository.findByName(permissionName);
        Set<Permission> permissions = new HashSet<>();
        permissions.add(permissione.get());
        rolee.get().setPermissions(permissions);
        roleRepository.save(rolee.get());
        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("UPDATE", performedBy, "ASSIGN ROLE "+ roleName+" TO PERMISSION: " +permissions);

    }
    @Override
    public void changeUserRole(String email, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        user.setRole(role);
        userRepository.save(user);
        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("UPDATE", performedBy, "Change user: " + roleName);
    }
    @Override
    public UserDTO getUserById(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist")));

        return UserMapper.UserToDto(user.get());
    }

    @Override
    public List<UserDTO> getAllUser() {
        List<User> users= userRepository.findAll();
        return users.stream()
                .map(UserMapper::UserToDto)
                .toList();
    }
    @Override
    public UserDTO updateUser(String email, User user) {
        Optional<User> userOptional= Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user not found")));;
        User updatedUser = userOptional.get();
        updatedUser.setEmail(user.getEmail());
        updatedUser.setFirstname(user.getFirstname());
        User savedUser=userRepository.save(updatedUser);
        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("UPDATE", performedBy, "Update User: " + email);
        return UserMapper.UserToDto(savedUser);
    }
    @Override
    public void deleteUser(String email) {
        Optional<User> userOptional= Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user not found")));
        User user= userOptional.get();
        user.setRole(null);
        userRepository.deleteById(user.getId());
        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("UPDATE", performedBy, "Update User: " + email);
    }

    @Override
    public void assignDepToUser(String email, String departementName) {
        if (!departmentRepository.existsByName(departementName)) {
            throw new ResourceNotFoundException("Departement does not  exists");
        }
        Department department=departmentRepository.findByName(departementName);

        Optional<User> userOpt= Optional.ofNullable(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        Optional<Employee> employee= Optional.ofNullable(employeeRepository.findById(userOpt.get().getEmployee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found")));
        employee.get().setDepartment(department);
        String performedBy = auditLogger.resolvePerformedBy();
        auditLogger.logAudit("UPDATE", performedBy, "ASSIGN Departement "+ departementName+" TO User: " +email);

    }
}

