package com.Ashish.Booking.Sytem.UserManagement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "email cannot be empty")
    @Email(message = "invalid email")
    private String email;

    @NotBlank(message = "password cannot be empty")
    private String password;
}
