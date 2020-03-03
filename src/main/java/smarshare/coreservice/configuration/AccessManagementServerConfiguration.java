package smarshare.coreservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "access")
public @Data
class AccessManagementServerConfiguration {

    private String hostName;
    private int port;
}
