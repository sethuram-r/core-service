package smarshare.coreservice.read.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.dto.ObjectMetadata;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.filestructure.FolderComponent;
import smarshare.coreservice.read.service.helper.BucketObjectsHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3ReadService {

    private AmazonS3 amazonS3Client;
    private BucketObjectsHelper bucketObjectsHelper;
    private ObjectMapper objectToJsonConverter;
    private AccessManagementAPIService accessManagementAPIService;



    @Autowired
    S3ReadService(AmazonS3 amazonS3Client, BucketObjectsHelper bucketObjectsHelper,
                  ObjectMapper objectToJsonConverter, AccessManagementAPIService accessManagementAPIService) {
        this.amazonS3Client = amazonS3Client;
        this.bucketObjectsHelper = bucketObjectsHelper;
        this.objectToJsonConverter = objectToJsonConverter;
        this.accessManagementAPIService = accessManagementAPIService;
    }

    public List<Bucket> listBuckets() {
        log.info( "Inside listBuckets" );
        List<com.amazonaws.services.s3.model.Bucket> rawBucketListFromS3 = amazonS3Client.listBuckets();
        return rawBucketListFromS3.stream()
                .map( bucket -> new Bucket( bucket.getName() ) )
                .collect( Collectors.toList() );
    }


    private Map<String, ObjectMetadata> getObjectMetaData(String bucketName, int userId) {
        return accessManagementAPIService.getAllBucketObjectMetadataByBucketNameAndUserId( bucketName, userId );
    }

    private Map<String, String> getObjectKeys(String bucketName) {

        final LinkedHashMap<String, String> collect = amazonS3Client.listObjectsV2( bucketName ).getObjectSummaries().stream()
                .collect( Collectors.toMap(
                        S3ObjectSummary::getKey,
                        objectSummary -> objectSummary.getLastModified().toLocaleString(),
                        (u, v) -> u,
                        LinkedHashMap::new )
                );

        System.out.println( "0------->" + collect.toString() );

        return collect;
    }

    public String listObjectsWithMetadata(int userId, String bucketName) {
        log.info( "Inside listObjectsWithMetadata" );
        try {
            Map<String, ObjectMetadata> objectsMetadata = getObjectMetaData( bucketName, userId );
            System.out.println( "objectsMetadata------------>" + objectsMetadata );
            FolderComponent completeFileStructure = bucketObjectsHelper.convertKeysInFileStructureFormat( getObjectKeys( bucketName ), bucketName, objectsMetadata );
            return objectToJsonConverter.writeValueAsString( completeFileStructure );
        } catch (Exception e) {
            log.error( "Exception while converting file structure into JSON " + e.getMessage() );
            return null;
        }
    }


    public S3Object getObject(String objectName, String bucketName) {
        log.info( "Inside getObject" );
        try {
            return amazonS3Client.getObject( bucketName, objectName );
        } catch (AmazonServiceException e) {
            log.error( e.getErrorMessage() );
        }
        return null;
    }
}
