package com.Ashish.Booking.Sytem.ScreenManagement;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequestDto {
    @NotBlank(message = "name cannot be blank")
    private String name;
    @NotNull(message = "theatre id is required")
    private UUID theatreId;

    @Min(value = 1, message = "total rows must be at least 1")
    @Max(value = 26, message = "total rows cannot exceed 26")
    private int totalRows;

    @Min(value = 1, message = "seats per row must be at least 1")
    private int seatsPerRow;
}
