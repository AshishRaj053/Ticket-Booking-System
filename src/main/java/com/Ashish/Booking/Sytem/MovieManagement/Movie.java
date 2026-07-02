package com.Ashish.Booking.Sytem.MovieManagement;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(nullable = false)
    private LocalDate releaseDate;
}
