package com.Ashish.Booking.Sytem.ShowSeatManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowSeatRepo extends JpaRepository<ShowSeat, UUID> {
    List<ShowSeat> findByShowId(UUID showId);

    Optional<ShowSeat> findByShowIdAndSeatId(UUID showId, UUID seatId);
}
