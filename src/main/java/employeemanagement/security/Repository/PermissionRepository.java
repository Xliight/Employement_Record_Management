package employeemanagement.security.Repository;

import employeemanagement.security.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    boolean existsByName(String employeeRead);
}
