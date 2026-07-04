package com.Ashish.Booking.Sytem.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreatedEvent {
   private UUID bookingId;

   private UUID showId;

   private UUID userId;

}
