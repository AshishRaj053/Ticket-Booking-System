package com.Ashish.Booking.Sytem.consumer;

import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.event.BookingCreatedEvent;
import com.Ashish.Booking.Sytem.paymentManagement.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private final PaymentService paymentService;
    public PaymentConsumer(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CREATED,
            groupId = "payment-group"
    )

    public void consume(BookingCreatedEvent event){
        paymentService.processPayment(event);
    }



}
