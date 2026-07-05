package com.Ashish.Booking.Sytem.producer;

import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.event.BookingCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingCancelledProducer {
    private static final Logger log =
            LoggerFactory.getLogger(BookingCancelledProducer.class);
    private final KafkaTemplate<String, BookingCancelledEvent> kafkaTemplate;
    public BookingCancelledProducer(KafkaTemplate<String,BookingCancelledEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        kafkaTemplate.send(KafkaTopics.BOOKING_CANCELLED, event.getBookingId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {

                        log.error(
                                "Failed to publish BookingCancelledEvent",
                                ex
                        );

                        return;
                    }
                    log.info(
                            "Published BookingCancelledEvent. Partition={}, Offset={}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );

                });
    }
}
