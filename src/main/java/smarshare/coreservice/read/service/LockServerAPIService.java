package smarshare.coreservice.read.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import smarshare.coreservice.configuration.LockServerConfiguration;


@Slf4j
@Service
public class LockServerAPIService {


    private final UriComponentsBuilder lockServerUrl;
    private final RestTemplate restTemplate;

    @Autowired
    LockServerAPIService(
            RestTemplate restTemplate,
            LockServerConfiguration lockServerConfiguration
    ) {
        this.restTemplate = restTemplate;
        this.lockServerUrl = UriComponentsBuilder.newInstance().scheme( "http" )
                .host( lockServerConfiguration.getHostName() )
                .port( lockServerConfiguration.getPort() );
    }

    public Boolean getLockStatusForGivenObject(String objectName) {
        log.info( "Inside getLockStatusForGivenObject" );
        UriComponents urlToAccessLockStatusForGivenObject = lockServerUrl.replacePath( "/lock-service/status/object" )
                .replaceQueryParam( "objectName", objectName ).build();
        return restTemplate.getForObject( urlToAccessLockStatusForGivenObject.toUriString(), Boolean.class );

    }

    public Boolean getLockStatusForGivenObjects(String objectName) {
        log.info( "Inside getLockStatusForGivenObjects" );
        UriComponents urlToAccessLockStatusForGivenObjects = lockServerUrl.replacePath( "/lock-service/status/objects" )
                .replaceQueryParam( "objectName", objectName ).build();
        return restTemplate.getForObject( urlToAccessLockStatusForGivenObjects.toUriString(), Boolean.class );
    }


}
