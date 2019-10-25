package smarshare.coreservice.read.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
public class LockServerAPIService {


    @Value("${lock.server.host}")
    private String lockServerHostAddress;
    @Value("${lock.server.port}")
    private String lockServerPort;

    private UriComponentsBuilder lockServerUrl = UriComponentsBuilder.newInstance().scheme( "http" ).host( lockServerHostAddress ).port( lockServerPort );

    private RestTemplate restTemplate;

    @Autowired
    LockServerAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Boolean getLockStatusForGivenObject(String objectName) {
        log.info( "Inside getLockStatusForGivenObject" );
        UriComponents urlToAccessLockStatusForGivenObject = lockServerUrl.path( "/status/object/{objectName}" ).build();
        System.out.println( "Generated URI------------>" + urlToAccessLockStatusForGivenObject.toUriString() );
        Boolean lockStatusForGivenObject = restTemplate.getForObject( urlToAccessLockStatusForGivenObject.toUriString(), Boolean.class );
        System.out.println( "lockStatusForGivenObject--------->" + lockStatusForGivenObject );
        return lockStatusForGivenObject;

    }

    public List<Boolean> getLockStatusForGivenObjects(List<String> objectNames) {
        log.info( "Inside getLockStatusForGivenObjects" );
        UriComponents urlToAccessLockStatusForGivenObjects = lockServerUrl.path( "/status/objects" ).replaceQueryParam( "objectNames", objectNames ).build();
        System.out.println( "Generated URI------------>" + urlToAccessLockStatusForGivenObjects.toUriString() );
        List<Boolean> lockStatusForGivenObjects = restTemplate.getForObject( urlToAccessLockStatusForGivenObjects.toUriString(), List.class );
        System.out.println( "lockStatusForGivenObjects--------->" + lockStatusForGivenObjects );
        return lockStatusForGivenObjects;
    }


}
