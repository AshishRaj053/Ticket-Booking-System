package com.Ashish.Booking.Sytem.ShowManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheatreShowResponseDto {

    private UUID showId;

    private UUID movieId;

    private String movieName;

    private UUID screenId;

    private String screenName;

    private LocalDate showDate;

    private LocalTime startTime;
}