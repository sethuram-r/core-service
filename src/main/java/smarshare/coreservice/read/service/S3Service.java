package smarshare.coreservice.read.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.filestructure.BASE64DecodedMultipartFile;
import smarshare.coreservice.read.service.helper.BucketObjectsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class S3Service {

    private AmazonS3 amazonS3Client;
    private BucketObjectsHelper bucketObjectsHelper;
    private ObjectMapper objectToJsonConverter;


    @Autowired
    S3Service(AmazonS3 amazonS3Client, BucketObjectsHelper bucketObjectsHelper, ObjectMapper objectToJsonConverter) {
        this.amazonS3Client = amazonS3Client;
        this.bucketObjectsHelper = bucketObjectsHelper;
        this.objectToJsonConverter = objectToJsonConverter;
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


    private List<String> getObjectKeys(String userName,String bucketName){
        List<String> objectKeysObtainedFromS3 = new ArrayList<>(  );
        List<S3ObjectSummary>  objectsInRawFormat = amazonS3Client.listObjectsV2(bucketName).getObjectSummaries();
        if(!objectsInRawFormat.isEmpty()){
            objectsInRawFormat.forEach( eachObject -> {
                objectKeysObtainedFromS3.add( eachObject.getKey() );
            } );
        }
        System.out.println(objectKeysObtainedFromS3);
        bucketObjectsHelper.convertKeysInFileStructureFormat( objectKeysObtainedFromS3, userName, bucketName);
        return null;
    }

    public void listObjects(String userName,String bucketName){
        log.info( "Inside listObjects" );
        getObjectKeys(userName, bucketName );
        //objectToJsonConverter.writeValueAsString(   getObjectKeys(userName, bucketName ))
    }


    public Map<String, Resource> getObject(String objectName, String fileName, String bucketName) {
        log.info( "Inside getObject" );

        // have to check parameters and flow
        try {
            S3Object downloadedObject = amazonS3Client.getObject( bucketName, objectName );
            byte[] downloadedObjectInByteArrayFormat = ByteStreams.toByteArray( downloadedObject.getObjectContent() );
            BASE64DecodedMultipartFile downloadedObjectInMultipartFile = new BASE64DecodedMultipartFile( downloadedObjectInByteArrayFormat );
            Map<String, Resource> downloadedFile = new HashMap<>();
            downloadedFile.put( objectName, downloadedObjectInMultipartFile.getResource() );
            return downloadedFile;
        } catch (AmazonServiceException e) {
            log.error( e.getErrorMessage() );
        } catch (IOException e) {
            log.error( e.getMessage() );
        }
        return null;
    }
}
