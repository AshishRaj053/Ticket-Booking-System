package com.Ashish.Booking.Sytem.producer;

import com.Ashish.Booking.Sytem.Config.KafkaTopics;
import com.Ashish.Booking.Sytem.event.RefundCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RefundEventProducer {
    private static final Logger log =
            LoggerFactory.getLogger(RefundEventProducer.class);
    private final KafkaTemplate<String, RefundCompletedEvent> kafkaTemplate;
    public RefundEventProducer(KafkaTemplate<String,RefundCompletedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publishRefundCompleted(RefundCompletedEvent event){
        kafkaTemplate.send(KafkaTopics.REFUND_COMPLETED,event.getBookingId().toString(),event)
                .whenComplete((result,ex)->{
                    if (ex != null) {

                        log.error(
                                "Failed to publish RefundCompletedEvent",
                                ex
                        );

                        return;
                    }
                    log.info(
                            "Published RefundCompletedEvent. Partition={}, Offset={}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );

                });
    }
}
