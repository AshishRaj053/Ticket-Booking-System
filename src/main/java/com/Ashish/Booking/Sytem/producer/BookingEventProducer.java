package com.Ashish.Booking.Sytem.producer;

import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.event.BookingCreatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventProducer {
    private static final Logger log =
            LoggerFactory.getLogger(BookingEventProducer.class);
    private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;
    public BookingEventProducer(KafkaTemplate<String,BookingCreatedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingCreated(BookingCreatedEvent event){
        kafkaTemplate.send(KafkaTopics.BOOKING_CREATED,event.getBookingId().toString(),event)
                .whenComplete((result,ex)->{
                        if (ex != null) {

                            log.error(
                                    "Failed to publish BookingCreatedEvent",
                                    ex
                            );

                            return;
                        }
                        log.info(
                                "Published BookingCreatedEvent. Partition={}, Offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );

                });
    }

}
