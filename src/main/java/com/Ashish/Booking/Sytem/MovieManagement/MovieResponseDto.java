package com.Ashish.Booking.Sytem.MovieManagement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
//@AllArgsConstructor
public class MovieResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String language;
    private Integer duration;
    private Genre genre;
    private LocalDate releaseDate;
}
