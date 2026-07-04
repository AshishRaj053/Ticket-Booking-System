package com.Ashish.Booking.Sytem.paymentManagement;


import com.Ashish.Booking.Sytem.event.BookingCreatedEvent;
import com.Ashish.Booking.Sytem.event.PaymentCompletedEvent;
import com.Ashish.Booking.Sytem.producer.PaymentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);



    private final PaymentEventProducer paymentEventProducer;
    public PaymentService(PaymentEventProducer paymentEventProducer){
        this.paymentEventProducer = paymentEventProducer;
    }

    public void processPayment(BookingCreatedEvent booking){
        log.info("Payment Started");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // Restore interrupt status
            log.error("Payment processing interrupted", e);
            throw new RuntimeException("Payment processing interrupted", e);
        }

        log.info("Payment Successful");
        paymentEventProducer.publishPaymentCompleted(new PaymentCompletedEvent(booking.getBookingId(),booking.getShowId(),booking.getUserId()));


    }
}
