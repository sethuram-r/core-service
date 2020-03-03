package smarshare.coreservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lock")
public @Data
class LockServerConfiguration {
    private String hostName;
    private int port;
}

