package employeemanagement.security.services.impl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import employeemanagement.security.DTO.UserDTO;
import employeemanagement.security.DTO.UserMapper;
import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.PermissionRepository;
import employeemanagement.security.Repository.RoleRepository;
import employeemanagement.security.Repository.UserRepository;
import employeemanagement.security.model.Permission;
import employeemanagement.security.model.Role;
import employeemanagement.security.model.User;

public class AdminServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddPermissionSuccess() {
        String permissionName = "VIEW_USERS";

        Permission permission = new Permission(permissionName);

        when(permissionRepository.existsByName(permissionName)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);
        when(auditLogger.resolvePerformedBy()).thenReturn("test_user");

        Permission createdPermission = adminService.addPermission(permissionName);


        verify(permissionRepository, times(1)).existsByName(permissionName);
        verify(permissionRepository, times(1)).save(any(Permission.class));
        verify(auditLogger, times(1)).logAudit(eq("CREATE"), eq("test_user"), eq("add Permission: VIEW_USERS"));

        assertNotNull(createdPermission);
        assertEquals(permissionName, createdPermission.getName());
    }

    @Test
    public void testAddPermissionAlreadyExists() {
        String permissionName = "EDIT_USERS";

        when(permissionRepository.existsByName(permissionName)).thenReturn(true);
        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                adminService.addPermission(permissionName));
        verify(permissionRepository, times(1)).existsByName(permissionName);
        verifyNoMoreInteractions(permissionRepository, auditLogger);
        assertEquals("Permission already exists", exception.getMessage());
    }

    @Test
    public void testAddRoleSuccess() {
        String roleName = "MANAGER";
        Role role = new Role(roleName);
        when(roleRepository.existsByName(roleName)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        Role createdRole = adminService.addRole(roleName);
        verify(roleRepository, times(1)).existsByName(roleName);
        verify(roleRepository, times(1)).save(any(Role.class));
        assertNotNull(createdRole);
        assertEquals(roleName, createdRole.getName());
    }

    @Test
    public void testAddRoleAlreadyExists() {
        String roleName = "ADMIN";
        when(roleRepository.existsByName(roleName)).thenReturn(true);
        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                adminService.addRole(roleName));

        verify(roleRepository, times(1)).existsByName(roleName);
        verifyNoMoreInteractions(roleRepository);
        assertEquals("Role already exists", exception.getMessage());
    }


    @Test
    public void testAssignPermissionSuccess() {
        String roleName = "ADMIN";
        String permissionName = "READ_PRIVILEGES";
        Role role = new Role(roleName);
        Permission permission = new Permission(permissionName);
        when(roleRepository.existsByName(roleName)).thenReturn(true);
        when(permissionRepository.existsByName(permissionName)).thenReturn(true);
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.of(permission));
        adminService.assignPermmission(roleName, permissionName);
        verify(roleRepository, times(1)).findByName(roleName);
        verify(permissionRepository, times(1)).findByName(permissionName);
        verify(roleRepository, times(1)).save(role);
        verify(auditLogger, times(1)).logAudit(any(), any(), any());
        assertTrue(role.getPermissions().contains(permission));
    }

    @Test
    public void testAssignPermissionRoleNotFound() {
        String roleName = "NON_EXISTENT_ROLE";
        String permissionName = "READ_PRIVILEGES";
        when(roleRepository.existsByName(roleName)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                adminService.assignPermmission(roleName, permissionName));
        assertEquals("Role does not  exists", exception.getMessage());
    }

    @Test
    public void testAssignPermissionPermissionNotFound() {
        String roleName = "ADMIN";
        String permissionName = "NON_EXISTENT_PERMISSION";
        when(roleRepository.existsByName(roleName)).thenReturn(true);
        when(permissionRepository.existsByName(permissionName)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                adminService.assignPermmission(roleName, permissionName));
        assertEquals("Permission does not  exists", exception.getMessage());
    }

    @Test
    public void testChangeUserRoleSuccess() {
        String email = "user@example.com";
        String roleName = "ADMIN";

        Role role = new Role(roleName);
        User user = new User();
        user.setEmail(email);
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        adminService.changeUserRole(email, roleName);
        verify(userRepository, times(1)).findByEmail(email);
        verify(roleRepository, times(1)).findByName(roleName);
        verify(userRepository, times(1)).save(user);
        assertEquals(role, user.getRole());
    }

    @Test
    public void testChangeUserRoleRoleNotFound() {
        String email = "user@example.com";
        String roleName = "NON_EXISTENT_ROLE";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                adminService.changeUserRole(email, roleName));

        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    public void testChangeUserRoleUserNotFound() {
        String email = "nonexistentuser@example.com";
        String roleName = "ADMIN";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(new Role(roleName)));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                adminService.changeUserRole(email, roleName));

        assertEquals("User does not exist", exception.getMessage());
    }

    @Test
    void testGetUserById_UserExists() {
        String email = "test@example.com";
        User mockUser = mock(User.class);
        mockUser.setId(1);
        mockUser.setEmail(email);
        UserDTO expectedDto = mock(UserDTO.class);

        mockStatic(UserMapper.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(UserMapper.UserToDto(mockUser)).thenReturn(expectedDto);

        UserDTO result = adminService.getUserById(email);

        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @Test
    void testGetUserById_UserDoesNotExist() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminService.getUserById(email);
        });

        assertEquals("User does not exist", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }


    @Test
    void testUpdateUser_UserNotFound() {
       
        String email = "nonexistent@example.com";
        User updateUser = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> adminService.updateUser(email, updateUser));
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_UserExists() {
        
        String email = "test@example.com";
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        adminService.deleteUser(email);

        
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).deleteById(existingUser.getId());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteUser(email));
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).deleteById(anyInt());
    }




}
