package employeemanagement.security.config;

import employeemanagement.security.Repository.EmployeeRepository;

import employeemanagement.security.model.Employee;
import employeemanagement.security.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final EmployeeRepository employeeRepository;
    @Override
    public boolean hasPermission(Authentication auth, Object targetId, Object permission) {
        if (!(auth.getPrincipal() instanceof User) || !(permission instanceof String)) {
            return false;
        }

        User user = (User) auth.getPrincipal();
        String perm = (String) permission;

        if ("VIEW_DEPARTMENT".equals(perm) || "UPDATE_DEPARTMENT".equals(perm)) {
            Long employeeId = (Long) targetId;
            Employee employee = employeeRepository.findById(employeeId).orElse(null);

            if (employee != null && user.getEmployee() != null) {
                return employee.getDepartment().equals(user.getEmployee().getDepartment());
            }
        }

        return false;
    }


    // Not implemented for this system
    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
