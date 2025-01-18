package employeemanagement.security.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue
    private Long id;


    @NotBlank(message = "Full Name is required")
    @Size(max = 100, message = "Full Name cannot exceed 100 characters")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Job Title is required")
    @Column(nullable = false)
    private String jobTitle;


    @NotNull(message = "Hire Date is required")
    @PastOrPresent(message = "Hire Date must be in the past or today")
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank(message = "Employment Status is required")
    @Column(nullable = false)
    private String employmentStatus;

    @NotBlank(message = "Contact Information is required")
    @Column(nullable = false)
    private String contactInfo;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Column(nullable = false)
    private String address;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Employee(long l, String johnDoe, String hrSpecialist, LocalDate of, String s, String s1, String s2, Department hr) {
        this.id=l;
        this.fullName=johnDoe;
        this.jobTitle=hrSpecialist;
        this.hireDate=of;
        this.employmentStatus=s;
        this.contactInfo=s1;
        this.address=s2;
        this.department=hr;

    }
}

