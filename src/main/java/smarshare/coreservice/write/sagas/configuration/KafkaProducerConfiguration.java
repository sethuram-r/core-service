package smarshare.coreservice.write.sagas.configuration;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import smarshare.coreservice.read.configuration.KafkaParameters;
import smarshare.coreservice.write.sagas.dto.SagaEventAccessManagementServiceWrapper;
import smarshare.coreservice.write.sagas.dto.SagaEventLockWrapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaProducerConfiguration {

    private final Map<String, Object> configurationProperties;

    private final KafkaParameters kafkaParameters;

    @Autowired
    KafkaProducerConfiguration(KafkaParameters kafkaParameters) {
        this.kafkaParameters = kafkaParameters;
        this.configurationProperties = new HashMap<>();
        configurationProperties.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaParameters.getUrl() );
        configurationProperties.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        configurationProperties.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class );
    }


    @Bean
    public ProducerFactory<String, SagaEventLockWrapper> lockProducerFactory() {
        return new DefaultKafkaProducerFactory<>( this.configurationProperties );
    }

    @Bean(name = "kafkaTemplateForLockServer")
    public KafkaTemplate<String, SagaEventLockWrapper> kafkaLockTemplate() {
        return new KafkaTemplate<>( lockProducerFactory() );
    }

    @Bean
    public ProducerFactory<String, SagaEventAccessManagementServiceWrapper> accessManagementProducerFactory() {
        return new DefaultKafkaProducerFactory<>( this.configurationProperties );
    }

    @Bean(name = "kafkaTemplateForAccessManagementServer")
    public KafkaTemplate<String, SagaEventAccessManagementServiceWrapper> kafkaAccessManagementTemplate() {
        return new KafkaTemplate<>( accessManagementProducerFactory() );
    }
}
