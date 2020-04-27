package smarshare.coreservice.write.sagas.configuration;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import smarshare.coreservice.write.sagas.dto.SagaEventAccessManagementServiceWrapper;
import smarshare.coreservice.write.sagas.dto.SagaEventLockWrapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfiguration {

    private Map<String, Object> configurationProperties;

    KafkaConsumerConfiguration() {
        this.configurationProperties = new HashMap<>();
        configurationProperties.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        configurationProperties.put( ConsumerConfig.GROUP_ID_CONFIG, "sagaConsumer" );
        configurationProperties.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true );
        configurationProperties.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
        configurationProperties.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class );
    }


    @Bean(name = "kafkaLockConsumer")
    public KafkaConsumer<String, SagaEventLockWrapper> kafkaLockConsumer() {
        return new KafkaConsumer<>( configurationProperties, new StringDeserializer(), new JsonDeserializer<>( SagaEventLockWrapper.class, false ) );
    }

    @Bean(name = "kafkaAccessManagementConsumer")
    public KafkaConsumer<String, SagaEventAccessManagementServiceWrapper> kafkaAccessManagementConsumer() {
        return new KafkaConsumer<>( configurationProperties, new StringDeserializer(), new JsonDeserializer<>( SagaEventAccessManagementServiceWrapper.class, false ) );
    }

}
