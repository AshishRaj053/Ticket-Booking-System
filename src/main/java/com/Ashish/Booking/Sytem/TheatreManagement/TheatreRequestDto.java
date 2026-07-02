package com.Ashish.Booking.Sytem.TheatreManagement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheatreRequestDto {
    @NotBlank(message = "name cannot be empty")
    private String name;
    @NotBlank(message = "location cannot be blank")
    private String location;
}
