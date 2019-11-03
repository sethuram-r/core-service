package smarshare.coreservice.read.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import smarshare.coreservice.read.dto.BucketMetadata;
import smarshare.coreservice.read.dto.ObjectMetadata;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AccessManagementAPIService {
    @Value("${access.server.host}")
    private String accessServerHostAddress;
    @Value("${access.server.port}")
    private String accessServerPort;

    private UriComponentsBuilder accessServerUrl = UriComponentsBuilder.newInstance().scheme( "http" ).host( accessServerHostAddress ).port( accessServerPort );

    private RestTemplate restTemplate;

    @Autowired
    AccessManagementAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, BucketMetadata>> fetchAccessDetailsForBuckets(String userName) {

        UriComponents url = accessServerUrl.path( "buckets/accessInfo" ).replaceQueryParam( "userName", userName ).build();
        System.out.println( "Generated URI------------>" + url.toUriString() );
        List<Map<String, BucketMetadata>> accessDetailsForBucketsOfGivenUser = restTemplate.getForObject( url.toUriString(), List.class );
        System.out.println( "accessDetailsForBucketsOfGivenUser--------->" + accessDetailsForBucketsOfGivenUser );
        return accessDetailsForBucketsOfGivenUser;
    }


    public List<Map<String, ObjectMetadata>> fetchAccessDetailsForObjectsInBucketForSpecificUser(String bucketName, String userName) {

        UriComponents url = accessServerUrl.path( "objects/accessInfo" ).replaceQueryParam( "userName", userName )
                .replaceQueryParam( "bucketName", bucketName ).build();
        System.out.println( "Generated URI------------>" + url.toUriString() );
        List<Map<String, ObjectMetadata>> accessDetailsForBucketsOfGivenUser = restTemplate.getForObject( url.toUriString(), List.class );
        System.out.println( "accessDetailsForBucketsOfGivenUser--------->" + accessDetailsForBucketsOfGivenUser );
        return accessDetailsForBucketsOfGivenUser;
    }

}
