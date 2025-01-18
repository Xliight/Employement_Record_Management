package employeemanagement.security.DTO;

import employeemanagement.security.model.User;

public class UserMapper {
    public static UserDTO UserToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole().getName());
        return userDTO;
    }
}
