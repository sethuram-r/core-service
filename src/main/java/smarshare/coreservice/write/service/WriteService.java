package smarshare.coreservice.write.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.File;
import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.model.Status;
import smarshare.coreservice.write.model.lock.Folder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WriteService {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private S3WriteService s3WriteService;



    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    WriteService(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter, S3WriteService s3WriteService) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
        this.s3WriteService = s3WriteService;
    }

    public Status createBucketInStorage(Bucket bucket) {
        log.info( "Inside createBucketInStorage" );
        Status createBucketStatus = s3WriteService.createBucket( bucket );
        if (createBucketStatus.getMessage().equals( "Success" )) {
            try {
                ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "read", "add", jsonConverter.writeValueAsString( bucket ) );
                if (!producerResult.get().getRecordMetadata().toString().isEmpty()) return createBucketStatus;
            } catch (Exception exception) {
                log.error( " Exception while publishing user to Kafka " + exception.getCause() + exception.getMessage() );
            }
            return createBucketStatus;
        }
        return createBucketStatus;
    }

    public Status deleteBucketInStorage(Bucket bucket) {
        log.info( "Inside deleteBucketInStorage" );
        Status deleteBucketStatus = s3WriteService.deleteBucket( bucket );
        if (deleteBucketStatus.getMessage().equals( "Success" )) {
            try {
                ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "read", "delete", jsonConverter.writeValueAsString( bucket ) );
                if (!producerResult.get().getRecordMetadata().toString().isEmpty()) return deleteBucketStatus;
            } catch (Exception exception) {
                log.error( " Exception while publishing user to Kafka " + exception.getCause() + exception.getMessage() );
            }
            return deleteBucketStatus;
        }
        return deleteBucketStatus;
    }


    public Status createEmptyFolder(smarshare.coreservice.write.model.Folder folder, String bucketName) {
        log.info( "Inside createEmptyFolder" );
        return s3WriteService.createObjectInSpecifiedBucket( folder, bucketName );
    }

    public Status deleteFileInStorage(File file, String bucketName) {
        log.info( "Inside deleteFileInStorage" );
        try {
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "file", jsonConverter.writeValueAsString( new smarshare.coreservice.write.model.lock.File( file.getFileName(), Boolean.TRUE ) ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty())
                return s3WriteService.deleteObject( file.getFileName(), bucketName );
        } catch (Exception exception) {
            log.error( " Exception while publishing lock to Kafka " + exception.getCause() + exception.getMessage() );
            Status status = new Status();
            status.setMessage( "Failed" );
            return status;
        }
        return null;
    }

    public Status deleteFolderInStorage(List<String> folderObjects, String bucketName) {
        log.info( "Inside deleteFolderInStorage" );
        try {
            String folderName = folderObjects.get( 0 ).replace( "/", " " ).trim();
            List<smarshare.coreservice.write.model.lock.File> objectsInTheFolder = new ArrayList<>();
            System.out.println( "folderName---------------->" + folderName );
            folderObjects.forEach( object -> {
                if (!object.contains( folderName )) {
                    objectsInTheFolder.add( new smarshare.coreservice.write.model.lock.File( object, Boolean.TRUE ) );
                }
            } );
            Folder folder = new Folder( folderName, objectsInTheFolder, Boolean.TRUE );
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "folder", jsonConverter.writeValueAsString( folder ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty())
                return s3WriteService.deleteObjects( folderObjects, bucketName );
        } catch (Exception exception) {
            log.error( " Exception while publishing lock to Kafka " + exception.getCause() + exception.getMessage() );
            Status status = new Status();
            status.setMessage( "Failed" );
            return status;
        }
        return null;
    }

    public void uploadFileToS3(FileToUpload fileToUpload) {
        log.info( "inside uploadFileToS3 " );
        //lock
        // uploading tos3
        s3WriteService.uploadObject( fileToUpload );
        // publish event to access management server
        //update local cache.

    }
}
