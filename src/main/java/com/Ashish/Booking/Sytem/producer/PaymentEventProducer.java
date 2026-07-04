package com.Ashish.Booking.Sytem.producer;

import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.event.BookingCreatedEvent;
import com.Ashish.Booking.Sytem.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {
    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventProducer.class);
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    public PaymentEventProducer(KafkaTemplate<String,PaymentCompletedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event){
        kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED,event.getBookingId().toString(),event)
                .whenComplete((result,ex)->{
                    if (ex != null) {

                        log.error(
                                "Failed to publish PaymentCompletedEvent",
                                ex
                        );

                        return;
                    }
                    log.info(
                            "Published PaymentCompletedEvent. Partition={}, Offset={}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );

                });
    }

}
