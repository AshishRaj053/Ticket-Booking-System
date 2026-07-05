package com.Ashish.Booking.Sytem.paymentManagement;


import com.Ashish.Booking.Sytem.event.BookingCancelledEvent;
import com.Ashish.Booking.Sytem.event.BookingCreatedEvent;
import com.Ashish.Booking.Sytem.event.PaymentCompletedEvent;
import com.Ashish.Booking.Sytem.event.RefundCompletedEvent;
import com.Ashish.Booking.Sytem.producer.PaymentEventProducer;
import com.Ashish.Booking.Sytem.producer.RefundEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);


    private final RefundEventProducer refundEventProducer;
    private final PaymentEventProducer paymentEventProducer;
    public PaymentService(PaymentEventProducer paymentEventProducer, RefundEventProducer refundEventProducer){
        this.paymentEventProducer = paymentEventProducer;
        this.refundEventProducer = refundEventProducer;
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

    public void refundProcess(BookingCancelledEvent event){
        log.info("Refund Started");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // Restore interrupt status
            log.error("Refund processing interrupted", e);
            throw new RuntimeException("Refund processing interrupted", e);
        }

        log.info("Refund Successful");
        refundEventProducer.publishRefundCompleted(new RefundCompletedEvent(event.getBookingId(),event.getShowId(),event.getUserId()));
    }

}
