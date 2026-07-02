package com.Ashish.Booking.Sytem.SeatManagement;

import com.Ashish.Booking.Sytem.ScreenManagement.Screen;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponseDto {

    private UUID id;
    private String rowName;
    private int seatNumber;
    private String label;
}
