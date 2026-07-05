package com.Ashish.Booking.Sytem.NotificationManagement;

import com.Ashish.Booking.Sytem.event.BookingCancelledEvent;
import com.Ashish.Booking.Sytem.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    public void sendBookingConfirmation(
            PaymentCompletedEvent event){

        log.info(
                "Notification sent to User {} for Booking {}",
                event.getUserId(),
                event.getBookingId()
        );
    }

    public void sendCancellationConfirmation(BookingCancelledEvent event){
        log.info(
                "Notification sent to User {} for cancellation {}",
                event.getUserId(),
                event.getBookingId()
        );
    }
}
