package com.Ashish.Booking.Sytem.ShowManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowRepo extends JpaRepository<Show, UUID> {
    List<Show> findByScreenIdAndShowDate(UUID screenId, LocalDate showDate);

    @Query("""
SELECT s
FROM Show s
WHERE s.movie.id = :movieId
AND (
        s.showDate > :today
        OR
        (s.showDate = :today AND s.startTime > :currentTime)
)
ORDER BY s.showDate ASC,
         s.startTime ASC
""")
    List<Show> findUpcomingShowsByMovie(
            @Param("movieId") UUID movieId,
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime
    );

    @Query("""
SELECT s
FROM Show s
WHERE s.screen.theatre.id = :theatreId
AND (
        s.showDate > :today
        OR
        (
            s.showDate = :today
            AND s.startTime > :currentTime
        )
)
ORDER BY
        s.showDate ASC,
        s.startTime ASC
""")
    List<Show> findUpcomingShowsByTheatre(
            @Param("theatreId") UUID theatreId,
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime
    );
}
