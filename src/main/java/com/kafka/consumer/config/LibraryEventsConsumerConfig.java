package com.kafka.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    private DefaultErrorHandler defaultErrorHandler() {
        //when exception occurred it will perform retry with 2 times of every 1 sec
        var fixedBackoff = new FixedBackOff(1000l, 2);
        var errorHandler = new DefaultErrorHandler(fixedBackoff);
        //when we want to monitor the exception do the below code
        errorHandler.setRetryListeners((record, ex, deliveryAttempts) -> {
            log.error("Failed Record in Retry Listener {}, Exception : {} and deliveryAttempts : {} ", record, ex, deliveryAttempts);
        });
        //when we want to ignore the exception to be retry in this case we can add below code.
        var exceptionToIgnoreList = List.of(IllegalArgumentException.class);
        exceptionToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);
        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Long, String>
    kafkaListenerContainerFactory(ConsumerFactory<Long, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3); //it's not recommended to use in cloud deployment
        factory.setCommonErrorHandler(defaultErrorHandler()); //for catching the exception
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); //setting manually
        return factory;
    }

    //this is the programmetci approach
    /**
     @Autowired private KafkaProperties properties;

     @Bean KafkaListenerContainerFactory<ConcurrentMessageListenerContainer < Integer, String>>
     kafkaListenerContainerFactory() {
     ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
     new ConcurrentKafkaListenerContainerFactory<>();
     // factory.setConsumerFactory(consumerFactory());
     factory.setConcurrency(3);
     factory.getContainerProperties().setPollTimeout(3000);
     return factory;
     }

     @Bean public ConsumerFactory<Integer, String> consumerFactory() {
     return new DefaultKafkaConsumerFactory<>(consumerConfigs());
     }

     @Bean public Map<String, Object> consumerConfigs() {
     Map<String, Object> props = new HashMap<>();
     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getConsumer().getBootstrapServers());
     //...
     return props;
     }
     */
}
