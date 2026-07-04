package com.Ashish.Booking.Sytem.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Component;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingTopic(){
        return TopicBuilder
                .name(KafkaTopics.BOOKING_CREATED)
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic paymentTopic(){
        return TopicBuilder
                .name(KafkaTopics.PAYMENT_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
