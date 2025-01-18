package employeemanagement.security.controllers;

import employeemanagement.security.DTO.UserDTO;
import employeemanagement.security.model.User;
import employeemanagement.security.services.IAdminService;
import employeemanagement.security.services.impl.AdminService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMINISTRATOR') ")
public class AdminController {

    private final IAdminService adminService;

    @PostMapping("/permissions")
    public ResponseEntity<String> createPermission(@RequestParam String permissionName) {
        adminService.addPermission(permissionName);
        return ResponseEntity.ok( "Permission added successfully" );
    }
    @PostMapping("/roles")
    public ResponseEntity<String> createRole(@RequestParam String roleName) {
        adminService.addRole(roleName);
        return ResponseEntity.ok( "Role added successfully" );
    }
    @PutMapping("/assignPermToRole")
    public ResponseEntity<String> assignPermmission(@RequestParam String roleName,@RequestParam String permissionName) {
        adminService.assignPermmission(roleName,permissionName);
        return ResponseEntity.ok( "Permission assinged to Role  successfully" );
    }
    @PutMapping("/assignDepToManager")
    public ResponseEntity<String> assignDepartementToUser(@RequestParam String email,@RequestParam String departement) {
        adminService.assignDepToUser(email,departement);
        return ResponseEntity.ok( "Depatement assinged to User  successfully" );
    }

    @PutMapping("/changerUserRole")
    public ResponseEntity<String> changeUserRole(@RequestParam String email,@RequestParam String roleName) {
        try{
            adminService.changeUserRole(email,roleName);
            return ResponseEntity.ok( "Role Changed Successfully" );
        }catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("An error occurred while updating the user", ex);
        }

    }
    @GetMapping("/users")
    public ResponseEntity<?> getUser(@RequestParam String email) {
        try {
        UserDTO user = adminService.getUserById(email);
        return ResponseEntity.ok(user);
    }
         catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adding an employee fail: " + ex.getMessage());
    }
    }
    @GetMapping("/users/all")
    public ResponseEntity<?> getAllUser() {
        try {
            List<UserDTO> user = adminService.getAllUser();
            return ResponseEntity.ok(user);
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adding an employee fail: " + ex.getMessage());
        }
    }
    @PutMapping("/users")
    public ResponseEntity<?> updateUser(@RequestParam String email, @RequestBody User user) {
        try {
            UserDTO updatedUser = adminService.updateUser(email,user);
            return ResponseEntity.ok(updatedUser);

        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adding an employee fail: " + ex.getMessage());
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@RequestParam String email) {
        adminService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }


}