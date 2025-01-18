package employeemanagement.security.services;

import employeemanagement.security.DTO.UserDTO;
import employeemanagement.security.model.Permission;
import employeemanagement.security.model.Role;
import employeemanagement.security.model.User;

import java.util.List;
import java.util.Optional;

public interface IAdminService {
    Permission addPermission(String permissionName);

    Role addRole(String roleName);

    void assignPermmission(String roleName, String permissionName);

    void changeUserRole(String email, String roleName);

    UserDTO getUserById(String email);


    List<UserDTO> getAllUser();

    UserDTO updateUser(String email, User user);


    void deleteUser(String email);


    void assignDepToUser(String email, String departementName);
}
