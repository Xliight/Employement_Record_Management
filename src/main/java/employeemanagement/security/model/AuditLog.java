package employeemanagement.security.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(CREATE|UPDATE|DELETE)$", message = "Action must be CREATE, UPDATE, or DELETE")
    @Column(nullable = false)
    private String action;

    @NotBlank(message = "Performed By is required")
    @Column(nullable = false)
    private String performedBy;

    @NotNull(message = "Timestamp is required")
    @PastOrPresent(message = "Timestamp must be in the past or present")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotBlank(message = "Details are required")
    @Size(max = 500, message = "Details cannot exceed 500 characters")
    @Column(nullable = false)
    private String details;
}
