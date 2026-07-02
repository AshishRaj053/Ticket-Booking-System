package com.Ashish.Booking.Sytem.ShowManagement;

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
public class ShowResponseDto {
    private UUID id;

    private String movieTitle;

    private String theatreName;

    private String screenName;

    private LocalDate showDate;

    private LocalTime startTime;

    private LocalTime endTime;
}
