package com.Ashish.Booking.Sytem.ScreenManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScreenResponseDto {
    private UUID id;
    private String name;
    private Integer totalRows;
    private Integer seatsPerRow;
    private UUID theatreId;
    private String theatreName;
}
