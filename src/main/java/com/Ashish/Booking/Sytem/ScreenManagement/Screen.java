package com.Ashish.Booking.Sytem.ScreenManagement;

import com.Ashish.Booking.Sytem.TheatreManagement.Theatre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Screen {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "theatre_id")
    private Theatre theatre;

    @Column(nullable = false)
    private int totalRows;
    @Column(nullable = false)
    private int seatsPerRow;
}
