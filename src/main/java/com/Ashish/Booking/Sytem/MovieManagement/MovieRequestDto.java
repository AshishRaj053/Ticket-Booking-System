package com.Ashish.Booking.Sytem.MovieManagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDto {

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "description cannot be empty")
    private String description;

   @NotBlank(message = "language cannot be empty")
    private String language;

   @NotNull(message = "duration cannot be empty")
   @Min(1)
   private Integer duration;

    @NotNull(message = "genre cannot be empty")
    private Genre genre;

    @NotNull(message = "release date cannot be empty")
    private LocalDate releaseDate;
}
