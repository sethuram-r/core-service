package smarshare.coreservice.read.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import smarshare.coreservice.configuration.AccessManagementServerConfiguration;
import smarshare.coreservice.read.dto.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccessManagementAPIService {

    private UriComponentsBuilder accessServerUrl;
    private RestTemplate restTemplate;

    @Autowired
    AccessManagementAPIService(RestTemplate restTemplate,
                               AccessManagementServerConfiguration accessManagementServerConfiguration
    ) {
        this.restTemplate = restTemplate;
        this.accessServerUrl = UriComponentsBuilder.newInstance().scheme( "http" )
                .host( accessManagementServerConfiguration.getHostName() )
                .port( accessManagementServerConfiguration.getPort() );
    }

    public List<BucketMetadata> getAllBucketsMetaDataByUserName(String userName) {
        log.info( "Inside getAllBucketsMetaDataByUserName" );
        try {
            UriComponents url = accessServerUrl.replacePath( "buckets/accessInfo" ).replaceQueryParam( "userName", userName ).build();
            BucketsMetadata accessDetailsForBucketsOfGivenUser = restTemplate.getForObject( url.toUriString(), BucketsMetadata.class );
            System.out.println( "accessDetailsForBucketsOfGivenUser--------->" + accessDetailsForBucketsOfGivenUser );
            return accessDetailsForBucketsOfGivenUser.getBucketsMetadata();
        } catch (Exception e) {
            log.error( "Exception while getAllBucketsMetaDataByUserName " + e );
        }
        return Collections.emptyList();
    }


    public Map<String, ObjectMetadata> getAllBucketObjectMetadataByBucketNameAndUserName(String bucketName, String userName) {
        log.info( "Inside getAllBucketObjectMetadataByBucketNameAndUserName" );
        try {
            UriComponents url = accessServerUrl.replacePath( "objects/accessInfo" ).replaceQueryParam( "userName", userName )
                    .replaceQueryParam( "bucketName", bucketName ).build();
            return Objects.requireNonNull( restTemplate.getForObject( url.toUriString(), BucketObjectsMetadata.class ) ).getBucketObjectsMetadata().stream()
                    .collect( Collectors.toMap( BucketObjectMetadata::getObjectName, BucketObjectMetadata::getObjectMetadata ) );
        } catch (Exception e) {
            log.error( "Exception while getAllBucketObjectMetadataByBucketNameAndUserName " + e );
        }

        return Collections.emptyMap();
    }

}

