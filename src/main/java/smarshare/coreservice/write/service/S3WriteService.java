package smarshare.coreservice.write.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import smarshare.coreservice.write.exception.BucketExistException;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.model.Folder;
import smarshare.coreservice.write.model.Status;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class S3WriteService {

    private AmazonS3 amazonS3Client;
    private Status status;
    private TransferManager transferManager;


    @Autowired
    S3WriteService(AmazonS3 amazonS3Client, Status status, TransferManager transferManager) {
        this.amazonS3Client = amazonS3Client;
        this.status = status;
        this.transferManager = transferManager;
    }

    public Status createBucket(Bucket bucket) {

        log.info( "Inside createBucket in S3Service" );

        if (amazonS3Client.doesBucketExistV2( bucket.getName() )) {
            throw new BucketExistException( bucket.getName() + " already exists!" );
        } else {
            try {
                amazonS3Client.createBucket( bucket.getName() );
                status.setMessage( "Success" );
                return status;
            } catch (AmazonS3Exception e) {
                log.error( e.getErrorMessage() );
                status.setMessage( "Failed" );
                return status;
            }
        }
    }

    public Status deleteBucket(Bucket bucket) {
        log.info( "Inside deleteBucket in S3Service" ); // un-versioned bucket

        try {
            List<S3ObjectSummary> objectsInGivenBucket = amazonS3Client.listObjects( bucket.getName() ).getObjectSummaries();
            if (!objectsInGivenBucket.isEmpty()) {
                objectsInGivenBucket.forEach( objectSummary -> {
                    amazonS3Client.deleteObject( bucket.getName(), objectSummary.getKey() );
                } );
            }
            amazonS3Client.deleteBucket( bucket.getName() );
            status.setMessage( "Success" );
            return status;
        } catch (Exception e) {
            log.error( "Exception while deleting the bucket------->" + e.getMessage() );
            status.setMessage( "Failed" );
            return status;
        }
    }

    private File getDummyFile() throws IOException {
        Resource resource = new ClassPathResource( "static/dummy" );
        return resource.getFile();
    }

    public Status createObjectInSpecifiedBucket(Folder folder, String bucketName) {
        log.info( "Inside createObjectInSpecifiedBucket" );
        try {
            amazonS3Client.putObject( new PutObjectRequest( bucketName, folder.getName(), getDummyFile() ) );
            status.setMessage( "Success" );
            return status;
        } catch (IOException e) {
            log.error( "Exception while creating empty folder in specified bucket------->" + e.getMessage() );
            status.setMessage( "Failed" );
            return status;
        }
    }

    public Status deleteObject(String objectName, String bucketName) {
        log.info( "Inside deleteObject" );
        try {
            amazonS3Client.deleteObject( bucketName, objectName );
            status.setMessage( "Success" );
            return status;
        } catch (Exception e) {
            log.error( "Exception while deleting object in specified bucket------->" + e.getMessage() );
            status.setMessage( "Failed" );
            return status;
        }
    }

    public Status deleteObjects(List<String> objectNames, String bucketName) {
        log.info( "Inside deleteObjects" );
        try {
            List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
            objectNames.forEach( key -> keys.add( new DeleteObjectsRequest.KeyVersion( key ) ) );
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest( bucketName ).withKeys( keys );
            DeleteObjectsResult result = amazonS3Client.deleteObjects( deleteObjectsRequest );
            System.out.println( result.getDeletedObjects().get( 0 ).getKey() );
            status.setMessage( "Success" );
            return status;
        } catch (AmazonServiceException e) {
            log.error( "Exception while deleting folder in specified bucket------->" + e.getMessage() );
            status.setMessage( "Failed" );
            return status;
        }
    }

    public void uploadObject(FileToUpload fileToUpload) {
        log.info( "Inside uploadObject" );
        ObjectMetadata metadata = new ObjectMetadata();
        byte[] base64DecodedContentInByteArray = Base64.getDecoder().decode( fileToUpload.getUploadedFileContent().split( "base64," )[1] );
        InputStream inputStreamOfFileContent = new ByteArrayInputStream( base64DecodedContentInByteArray );
        metadata.setContentLength( base64DecodedContentInByteArray.length );
        PutObjectRequest request = new PutObjectRequest(
                fileToUpload.getBucketName(),
                fileToUpload.getSelectedFolderWhereFolderHasToBeUploaded() + fileToUpload.getUploadedFileName(),
                inputStreamOfFileContent, metadata );
        Upload uploadedObject = transferManager.upload( request );
        uploadedObject.addProgressListener( (ProgressListener) progressEvent -> log.info( "Transferred bytes of object " + fileToUpload.getUploadedFileName() + " : " + progressEvent.getBytesTransferred() ) );
    }



}
