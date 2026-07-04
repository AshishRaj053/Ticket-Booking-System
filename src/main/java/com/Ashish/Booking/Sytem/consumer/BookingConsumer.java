package com.Ashish.Booking.Sytem.consumer;

import com.Ashish.Booking.Sytem.BookingManagement.BookingService;
import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.event.PaymentCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingConsumer {
    private final BookingService bookingService;

    public BookingConsumer(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "booking-group"
    )

    public void consume(PaymentCompletedEvent event){
        bookingService.confirmBooking(event.getBookingId());
    }

}
