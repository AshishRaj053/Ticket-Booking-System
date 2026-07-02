package com.Ashish.Booking.Sytem.ShowSeatManagement;

import com.Ashish.Booking.Sytem.SeatManagement.Seat;
import com.Ashish.Booking.Sytem.ShowManagement.Show;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ShowSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;
}
