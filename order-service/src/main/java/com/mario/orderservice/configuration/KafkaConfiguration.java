package com.mario.orderservice.configuration;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaBackoffException;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;

@Configuration
public class KafkaConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfiguration.class);



    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> listenerContainer(KafkaProperties kafkaProperties) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(kafkaEventsConsumerFactory(kafkaProperties));

        factory.setRecordInterceptor(new RecordInterceptor<>() {
            @Override
            public ConsumerRecord<String, Object> intercept(ConsumerRecord<String, Object> consumerRecord, Consumer<String, Object> consumer) {
                return consumerRecord;
            }

            @Override
            public void failure(ConsumerRecord<String, Object> record, Exception exception, Consumer<String, Object> consumer) {
                if(ExceptionUtils.indexOfThrowable(exception, KafkaBackoffException.class) != -1) {
                    logger.debug("KafkaBackoffException thrown");
                } else {
                    logger.error("Error while processing record: {}\ntopic: {}, partition: {}, offset: {}, key: {}, Exception: {}",
                            record.value(), record.topic(), record.partition(), record.offset(), record.key(), exception);
                }
            }
        });
        return factory;
    }

    public ConsumerFactory<String, Object> kafkaEventsConsumerFactory(KafkaProperties kafkaProperties) {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServer());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaProperties.getSecurityProtocol());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, kafkaProperties.getTrustedPackages());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumerGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class); //JsonDeserializer

        return new DefaultKafkaConsumerFactory<>(props);
    }

}
