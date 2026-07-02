package com.Ashish.Booking.Sytem.BookedSeatManagement;

import com.Ashish.Booking.Sytem.SeatManagement.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookedSeatRepo extends JpaRepository<BookedSeat, UUID> {
    List<BookedSeat> findAllByBookingId(UUID bookingId);
}
