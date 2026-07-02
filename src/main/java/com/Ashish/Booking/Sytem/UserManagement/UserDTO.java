package com.Ashish.Booking.Sytem.UserManagement;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    @NotBlank(message = "name cannot be blank")
    private String name;

    @Email(message = "invalid format")
    @NotBlank(message = "email cannot be blank")
    private String email;

    @Size(min = 6,message = "password should be at least 6 characters")
    private String password;
}
