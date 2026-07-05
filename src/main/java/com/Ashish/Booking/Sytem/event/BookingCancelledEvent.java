package com.Ashish.Booking.Sytem.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCancelledEvent {
  private UUID bookingId;

   private UUID userId;

   private UUID showId;
}
