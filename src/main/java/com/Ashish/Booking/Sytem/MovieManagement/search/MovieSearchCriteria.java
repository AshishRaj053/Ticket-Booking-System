package com.Ashish.Booking.Sytem.MovieManagement.search;

import com.Ashish.Booking.Sytem.MovieManagement.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieSearchCriteria {
    private String name;

    private Genre genre;

    private String language;

    private Integer maxDuration;

    private LocalDate releasedAfter;

}
