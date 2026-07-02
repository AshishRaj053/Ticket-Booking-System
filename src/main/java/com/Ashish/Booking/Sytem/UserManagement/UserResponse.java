package com.Ashish.Booking.Sytem.UserManagement;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
}
