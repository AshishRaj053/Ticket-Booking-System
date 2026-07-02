package com.Ashish.Booking.Sytem.BookingManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDto {
    private UUID bookingId;

    private String movieTitle;

    private String theatreName;

    private String screenName;

    private LocalDate showDate;

    private LocalTime startTime;

    private BookingStatus status;

    private LocalDateTime bookingTime;

    private List<String> seats;
}
