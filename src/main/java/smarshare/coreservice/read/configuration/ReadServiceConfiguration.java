package smarshare.coreservice.read.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReadServiceConfiguration {

    @Bean
    public ObjectMapper objectToJsonConverter() {
        return new ObjectMapper();
    }

}
