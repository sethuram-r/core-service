package smarshare.coreservice.write.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.UploadObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3WriteService {

    private AmazonS3 amazonS3Client;
    private TransferManager transferManager;


    @Autowired
    S3WriteService(AmazonS3 amazonS3Client, TransferManager transferManager) {
        this.amazonS3Client = amazonS3Client;
        this.transferManager = transferManager;
    }


    public Boolean doesBucketExist(String bucketName) {
        log.info( "Inside doesBucketExist in S3Service" );
        return amazonS3Client.doesBucketExistV2( bucketName );
    }

    public Boolean createBucket(Bucket bucket) {
        try {
            log.info( "Inside createBucket in S3Service" );

            CreateBucketRequest createBucketRequest = new CreateBucketRequest( bucket.getBucketName(), "eu-west-1" )
                    .withCannedAcl( CannedAccessControlList.BucketOwnerFullControl );
            final com.amazonaws.services.s3.model.Bucket createdBucket = amazonS3Client.createBucket( createBucketRequest );

            return Boolean.TRUE;

        } catch (AmazonS3Exception e) {
            log.error( e.getMessage() );
        }
        return Boolean.FALSE;
    }

    public Boolean deleteBucket(String bucketName) {
        log.info( "Inside deleteBucket in S3Service" ); // un-versioned bucket

        try {
            List<S3ObjectSummary> objectsInGivenBucket = amazonS3Client.listObjects( bucketName ).getObjectSummaries();
            if (!objectsInGivenBucket.isEmpty()) {
                List<DeleteObjectsRequest.KeyVersion> objectKeys = objectsInGivenBucket.stream()
                        .map( objectSummary -> new DeleteObjectsRequest.KeyVersion( objectSummary.getKey() ) )
                        .collect( Collectors.toList() );

                DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest( bucketName )
                        .withKeys( objectKeys )
                        .withQuiet( false );

                DeleteObjectsResult deleteResult = amazonS3Client.deleteObjects( multiObjectDeleteRequest );
                log.info( deleteResult.getDeletedObjects().size() + " : Bucket Objects Deleted" );
            }
            amazonS3Client.deleteBucket( bucketName );
            return true;
        } catch (Exception e) {
            log.error( "Exception while deleting the bucket " + bucketName + e.getMessage() );
        }
        return false;
    }

    private File getDummyFile() throws IOException {
        Resource resource = new ClassPathResource( "static/dummy" );
        return resource.getFile();
    }

    public Boolean createObjectInBucket(UploadObject emptyFolder) {
        log.info( "Inside createObjectInSpecifiedBucket" );
        amazonS3Client.putObject( emptyFolder.getBucketName(), emptyFolder.getObjectName(), "" );
        return Boolean.TRUE;
    }

    public Boolean deleteObject(String objectName, String bucketName) {
        log.info( "Inside deleteObject" );
        try {
            amazonS3Client.deleteObject( bucketName, objectName );
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error( "Exception while deleting object in specified bucket------->" + e.getMessage() );
        }
        return Boolean.FALSE;
    }

    public DeleteObjectsResult deleteObjects(List<String> objectNames, String bucketName) {
        log.info( "Inside deleteObjects" );
        try {

            List<DeleteObjectsRequest.KeyVersion> objectKeys = objectNames.stream()
                    .map( DeleteObjectsRequest.KeyVersion::new )
                    .collect( Collectors.toList() );

            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest( bucketName )
                    .withKeys( objectKeys )
                    .withQuiet( false );
            return amazonS3Client.deleteObjects( deleteObjectsRequest );
        } catch (AmazonServiceException e) {
            log.error( "Exception while deleting folder in specified bucket------->" + e.getMessage() );
        }
        return null;
    }

    public Transfer.TransferState uploadObject(UploadObject uploadObject) {
        log.info( "Inside uploadObject" );
        try {

            byte[] bytes = Base64.getDecoder().decode( uploadObject.getContent() );
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength( bytes.length );
            PutObjectRequest request = new PutObjectRequest(
                    uploadObject.getBucketName(),
                    uploadObject.getObjectName(),
                    new ByteArrayInputStream( bytes ), metadata );

            Upload uploadedObject = transferManager.upload( request );

            uploadedObject.addProgressListener( (ProgressListener) progressEvent -> {
                log.info( "Transferred bytes of object " + uploadObject.getObjectName() + " : " + progressEvent.getBytesTransferred() );
            } );
            uploadedObject.waitForUploadResult();

            log.info( "Transfer of " + uploadedObject.getState() );

            return uploadedObject.getState();
        } catch (InterruptedException e) {
            log.error( String.format( "Exception occurred while uploading %s", e.getMessage() ) );
        }
        return Transfer.TransferState.Failed;
    }

    public Boolean uploadObjects(List<UploadObject> uploadObjects) {
        try {
            long resultCount = uploadObjects.stream()
                    .map( this::uploadObject )
                    .filter( transferState -> transferState == Transfer.TransferState.Completed )
                    .count();
            if (resultCount == uploadObjects.size()) return Boolean.TRUE;
        } catch (Exception e) {
            log.error( String.format( "Exception occurred while uploading %s", e ) );
        }
        return Boolean.FALSE;
    }

    public List<S3ObjectSummary> listObjectsByPrefix(String objectName, String bucketName) {

        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName( bucketName )
                .withPrefix( objectName );
        return amazonS3Client.listObjectsV2( listObjectsRequest ).getObjectSummaries();

    }


}
