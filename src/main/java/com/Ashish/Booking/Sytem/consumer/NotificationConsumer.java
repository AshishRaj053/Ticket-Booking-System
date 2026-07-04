package com.Ashish.Booking.Sytem.consumer;

import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.NotificationManagement.NotificationService;
import com.Ashish.Booking.Sytem.event.PaymentCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private final NotificationService notificationService;

    public NotificationConsumer(
            NotificationService notificationService){

        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "notification-group"
    )
    public void consume(PaymentCompletedEvent event){

        notificationService.sendBookingConfirmation(event);

    }

}
