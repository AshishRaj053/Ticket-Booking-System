package com.Ashish.Booking.Sytem.ShowManagement;

import com.Ashish.Booking.Sytem.MovieManagement.Movie;
import com.Ashish.Booking.Sytem.ScreenManagement.Screen;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowRequestDto {

    @NotNull(message = "movie id cannot be blank")
    private UUID movieId;
    @NotNull(message = "screen id cannot be blank")
    private UUID screenId;

    @NotNull(message = "show date is required")
    @FutureOrPresent(message = "show date cannot be in the past")
    private LocalDate showDate;

    @NotNull(message = "start time cannot be null")
    private LocalTime startTime;
}
