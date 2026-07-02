package com.Ashish.Booking.Sytem.SeatManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepo extends JpaRepository<Seat, UUID> {
    List<Seat> findByScreenId(UUID screenId);
}
