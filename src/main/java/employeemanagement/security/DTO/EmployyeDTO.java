package employeemanagement.security.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployyeDTO {

    private String fullName;
    private String jobTitle;
    private LocalDate hireDate;
    private String employmentStatus;
    private String contactInfo;
    private String address;
    private String department;
    private String user;



}
