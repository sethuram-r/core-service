package smarshare.coreservice.write.sagas.configuration;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import smarshare.coreservice.write.sagas.dto.SagaEventAccessManagementServiceWrapper;
import smarshare.coreservice.write.sagas.dto.SagaEventLockWrapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    private Map<String, Object> configurationProperties;

    KafkaProducerConfiguration() {
        this.configurationProperties = new HashMap<>();
        configurationProperties.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        configurationProperties.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        configurationProperties.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class );
    }


    @Bean
    public ProducerFactory<String, SagaEventLockWrapper> lockProducerFactory() {
        return new DefaultKafkaProducerFactory<>( this.configurationProperties );
    }

    @Bean
    public KafkaTemplate<String, SagaEventLockWrapper> kafkaLockTemplate() {
        return new KafkaTemplate<>( lockProducerFactory() );
    }

    @Bean
    public ProducerFactory<String, SagaEventAccessManagementServiceWrapper> accessManagementProducerFactory() {
        return new DefaultKafkaProducerFactory<>( this.configurationProperties );
    }

    @Bean
    public KafkaTemplate<String, SagaEventAccessManagementServiceWrapper> kafkaAccessManagementTemplate() {
        return new KafkaTemplate<>( accessManagementProducerFactory() );
    }
}
