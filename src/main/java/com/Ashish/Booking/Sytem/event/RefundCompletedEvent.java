package com.Ashish.Booking.Sytem.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundCompletedEvent {
    private UUID bookingId;

    private UUID showId;

    private UUID userId;
}
