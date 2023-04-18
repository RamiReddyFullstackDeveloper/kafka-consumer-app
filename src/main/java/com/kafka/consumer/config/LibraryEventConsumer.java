package com.kafka.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @link https://docs.spring.io/spring-kafka/reference/html/#receiving-messages
 */
//@Configuration
@Slf4j
public class LibraryEventConsumer {


    //this approach: when we depends on the default commiting offset
    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Long, String> consumerRecord) {
        log.info("Consume messages from Kafka Topic {} ", consumerRecord);
    }

}

//pls make sure that you have to enable either of one configuration only, don't enable two at a time,
// so here am commenting the line number 13 for my testing and using 30line number. i just given an example with two diff configurations.
@Configuration
@Slf4j
class LibraryEventConsumerManualCommmitOffset implements AcknowledgingMessageListener<Long, String>{

    @Override
    public void onMessage(ConsumerRecord<Long, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("Consume messages from Kafka Topic {} ", consumerRecord);
        acknowledgment.acknowledge();
    }
}