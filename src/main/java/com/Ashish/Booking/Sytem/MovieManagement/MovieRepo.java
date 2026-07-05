package com.Ashish.Booking.Sytem.MovieManagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface MovieRepo extends JpaRepository<Movie, UUID>, JpaSpecificationExecutor<Movie> {
    Page<Movie> findByReleaseDateAfter(LocalDate releaseDate, Pageable pageable);

    @Query("""
SELECT DISTINCT m
FROM Movie m
JOIN Show s
ON s.movie.id = m.id
WHERE s.showDate >= :today
""")
    Page<Movie> findNowShowing(@Param("today")LocalDate now, Pageable pageable);

    @Query("""
SELECT b.show.movie
FROM Booking b
WHERE b.status = 'CONFIRMED'
GROUP BY b.show.movie
ORDER BY COUNT(b) DESC
""")
    Page<Movie> findTrendingMovies(Pageable pageable);
}
