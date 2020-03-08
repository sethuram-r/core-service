package smarshare.coreservice.write.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WriteServiceConfiguration {

    @Bean
    public ObjectWriter customObjectMapperForKafka() {
        return new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

}
