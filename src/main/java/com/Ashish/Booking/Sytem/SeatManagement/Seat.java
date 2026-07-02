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
@Entity
public class Seat {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;
    @Column(nullable = false)
    private String rowName;
    @Column(nullable = false)
    private int seatNumber;

    @ManyToOne
    @JoinColumn(name = "screen_id")
    private Screen screen;
}
