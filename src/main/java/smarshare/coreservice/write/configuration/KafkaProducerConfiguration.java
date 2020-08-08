package smarshare.coreservice.write.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import smarshare.coreservice.read.configuration.KafkaParameters;

import java.util.HashMap;
import java.util.Map;

@Configuration(value = "kafkaWrite")
public class KafkaProducerConfiguration {

    private final KafkaParameters kafkaParameters;

    public KafkaProducerConfiguration(KafkaParameters kafkaParameters) {
        this.kafkaParameters = kafkaParameters;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                this.kafkaParameters.getUrl() );
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class );
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class );
        return new DefaultKafkaProducerFactory<>( configProps );
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>( producerFactory() );
    }
}
