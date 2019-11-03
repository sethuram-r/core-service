package smarshare.coreservice.read.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.dto.ObjectMetadata;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.S3DownloadObject;
import smarshare.coreservice.read.model.S3DownloadedObject;
import smarshare.coreservice.read.model.filestructure.BASE64DecodedMultipartFile;
import smarshare.coreservice.read.model.filestructure.FolderComponent;
import smarshare.coreservice.read.service.helper.BucketObjectsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Bucket> listBuckets(){
        log.info( "Inside listBuckets" );
        List<Bucket> bucketList  = new ArrayList<>();
        List<com.amazonaws.services.s3.model.Bucket> rawBucketListFromS3 = amazonS3Client.listBuckets();
        if(!rawBucketListFromS3.isEmpty()){
            rawBucketListFromS3.forEach( bucket -> {
                bucketList.add( new Bucket(bucket.getName()  ) );
            } );
        }
        return bucketList;
    }


    private List<Map<String, ObjectMetadata>> getObjectMetaData(String bucketName, String userName) {
        return accessManagementAPIService.fetchAccessDetailsForObjectsInBucketForSpecificUser( bucketName, userName );
    }

    private List<String> getObjectKeys(String bucketName) {
        List<String> objectKeysObtainedFromS3 = new ArrayList<>(  );
        List<S3ObjectSummary>  objectsInRawFormat = amazonS3Client.listObjectsV2(bucketName).getObjectSummaries();
        if(!objectsInRawFormat.isEmpty()){
            objectsInRawFormat.forEach( eachObject -> {
                objectKeysObtainedFromS3.add( eachObject.getKey() );
            } );
        }
        System.out.println(objectKeysObtainedFromS3);
        return objectKeysObtainedFromS3;
    }

    public String listObjectsWithMetadata(String userName, String bucketName) {
        log.info( "Inside listObjectsWithMetadata" );
        try {
            FolderComponent completeFileStructure = bucketObjectsHelper.convertKeysInFileStructureFormat( getObjectKeys( bucketName ), bucketName, getObjectMetaData( bucketName, userName ) );
            return objectToJsonConverter.writeValueAsString( completeFileStructure );
        } catch (Exception e) {
            log.error( "Exception while converting file structure into JSON " + e.getMessage() );
            return "";
        }
    }


    public S3DownloadedObject getObject(S3DownloadObject s3DownloadObject) {
        log.info( "Inside getObject" );

        // have to check parameters and flow
        try {
            S3Object downloadedObject = amazonS3Client.getObject( s3DownloadObject.getBucketName(), s3DownloadObject.getObjectName() );
            byte[] downloadedObjectInByteArrayFormat = ByteStreams.toByteArray( downloadedObject.getObjectContent() );
            BASE64DecodedMultipartFile downloadedObjectInMultipartFile = new BASE64DecodedMultipartFile( downloadedObjectInByteArrayFormat );

            return new S3DownloadedObject( s3DownloadObject, downloadedObjectInMultipartFile.getResource() );
        } catch (AmazonServiceException e) {
            log.error( e.getErrorMessage() );
        } catch (IOException e) {
            log.error( e.getMessage() );
        }
        return null;
    }
}
