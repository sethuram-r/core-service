package smarshare.coreservice.write.service;

import com.amazonaws.services.s3.transfer.Transfer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.write.dto.BucketObjectForEvent;
import smarshare.coreservice.write.helper.CacheDeleteThread;
import smarshare.coreservice.write.helper.CacheUpdateThread;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.File;
import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.model.Status;
import smarshare.coreservice.write.model.lock.S3Object;
import smarshare.coreservice.write.model.lock.S3ObjectsWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WriteService {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private S3WriteService s3WriteService;
    private CacheManager cacheManager;



    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    WriteService(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter, CacheManager cacheManager,
                 S3WriteService s3WriteService) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
        this.s3WriteService = s3WriteService;
        this.cacheManager = cacheManager;
    }

    public Status createBucketInStorage(Bucket bucket) {
        log.info( "Inside createBucketInStorage" );
        Status createBucketStatus = s3WriteService.createBucket( bucket );
        if (createBucketStatus.getMessage().equals( "Success" )) {
            try {
                ListenableFuture<SendResult<String, String>> isAccessInfoEventSent = kafkaTemplate.send( "AccessManagement", "createBucket", jsonConverter.writeValueAsString( bucket.getName() ) );
                ListenableFuture<SendResult<String, String>> isUpdatingTheCacheEventForReadServerSent = kafkaTemplate.send( "read", "add", jsonConverter.writeValueAsString( bucket ) );
                if (!(isUpdatingTheCacheEventForReadServerSent.get().getRecordMetadata().toString().isEmpty()
                        && isAccessInfoEventSent.get().getRecordMetadata().toString().isEmpty()))
                    return createBucketStatus;
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
                ListenableFuture<SendResult<String, String>> isAccessInfoEventSent = kafkaTemplate.send( "AccessManagement", "deleteBucket", jsonConverter.writeValueAsString( bucket.getName() ) );
                ListenableFuture<SendResult<String, String>> isUpdatingTheCacheEventForReadServerSent = kafkaTemplate.send( "read", "delete", jsonConverter.writeValueAsString( bucket ) );
                if (!(isUpdatingTheCacheEventForReadServerSent.get().getRecordMetadata().toString().isEmpty()
                        && isAccessInfoEventSent.get().getRecordMetadata().toString().isEmpty()))
                    return deleteBucketStatus;
            } catch (Exception exception) {
                log.error( " Exception while publishing user to Kafka " + exception.getCause() + exception.getMessage() );
            }
            return deleteBucketStatus;
        }
        return deleteBucketStatus;
    }

    /*   -------------------------------------    Bucket Object Methods   ------------------------------------        */

    public Status createEmptyFolder(smarshare.coreservice.write.model.Folder folder, String bucketName, String owner) {
        log.info( "Inside createEmptyFolder" );
        Status status = s3WriteService.createObjectInSpecifiedBucket( folder, bucketName );
        try {
            if (status.getMessage().equals( "Success" )) {
                BucketObjectForEvent bucketObjectForEvent = new BucketObjectForEvent();
                bucketObjectForEvent.setBucketName( bucketName );
                bucketObjectForEvent.setObjectName( folder.getName() );
                bucketObjectForEvent.setOwnerName( owner );
                bucketObjectForEvent.setUserName( owner );
                List<BucketObjectForEvent> bucketObjectForEvents = new ArrayList<>();
                bucketObjectForEvents.add( bucketObjectForEvent );
                kafkaTemplate.send( "AccessManagement", "emptyBucketObject", jsonConverter.writeValueAsString( bucketObjectForEvents ) );
            }
        } catch (JsonProcessingException e) {
            log.error( "Exception while publishing createEmptyFolder event " + e.getMessage() );
        }
        return status;
    }

    private BucketObjectForEvent mapBucketObjectToBucketObjectEvent(String objectName, String bucketName) {
        BucketObjectForEvent bucketObjectForDeleteEvent = new BucketObjectForEvent();
        bucketObjectForDeleteEvent.setBucketName( bucketName );
        bucketObjectForDeleteEvent.setObjectName( objectName );
        return bucketObjectForDeleteEvent;
    }


    private void deleteObjectInCache(String fileName) {
        log.info( "Inside deleteObjectInCache" );
        if (cacheManager.checkWhetherObjectExistInCache( fileName )) {
            CacheDeleteThread cacheUpdateThread = new CacheDeleteThread( fileName );
            cacheUpdateThread.thread.start();
        }
    }

    public Status deleteFileInStorage(File file, String bucketName) {
        log.info( "Inside deleteFileInStorage" );
        try {
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "object", jsonConverter.writeValueAsString( new S3Object( file.getFileName(), Boolean.TRUE ) ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {
                Status status = s3WriteService.deleteObject( file.getFileName(), bucketName );
                if (status.getMessage().equals( "Success" )) {
                    List<BucketObjectForEvent> bucketObjectForDeleteEvents = new ArrayList<>();
                    bucketObjectForDeleteEvents.add( mapBucketObjectToBucketObjectEvent( file.getFileName(), bucketName ) );
                    kafkaTemplate.send( "AccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( bucketObjectForDeleteEvents ) );
                    deleteObjectInCache( file.getFileName() );
                    return status;
                }
            }

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
            List<S3Object> objectsToBeLocked = new ArrayList<>();
            if (null != folderObjects) {
                folderObjects.forEach( object -> {
                    objectsToBeLocked.add( new S3Object( object, Boolean.TRUE ) );
                } );
            } else {
                throw new NullPointerException( "Empty Objects Sent For Locking Operation" );
            }
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "objects", jsonConverter.writeValueAsString( new S3ObjectsWrapper( objectsToBeLocked ) ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {
                Status status = s3WriteService.deleteObjects( folderObjects, bucketName );
                if (status.getMessage().equals( "Success" )) {
                    List<BucketObjectForEvent> bucketObjectsForDeleteEvent = folderObjects.stream().map( folderObject -> mapBucketObjectToBucketObjectEvent( folderObject, bucketName ) ).collect( Collectors.toList() );
                    kafkaTemplate.send( "AccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( bucketObjectsForDeleteEvent ) );
                    folderObjects.forEach( this::deleteObjectInCache );
                    return status;
                }
            }
        } catch (Exception exception) {
            log.error( " Exception while publishing lock to Kafka " + exception.getCause() + exception.getMessage() );
            Status status = new Status();
            status.setMessage( "Failed" );
            return status;
        }
        return null;
    }

    private ListenableFuture<SendResult<String, String>> lockTheGivenObjects(List<FileToUpload> filesToUpload) {
        List<S3Object> objectsToBeLocked = new ArrayList<>();
        try {
            filesToUpload.forEach( fileToUpload -> {
                objectsToBeLocked.add( new S3Object( fileToUpload.getUploadedFileName(), Boolean.TRUE ) );
            } );
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "objects", jsonConverter.writeValueAsString( new S3ObjectsWrapper( objectsToBeLocked ) ) );
            return producerResult;
        } catch (JsonProcessingException e) {
            log.error( " Exception while publishing lock to Kafka " + e.getCause() + e.getMessage() );
        }
        return null;
    }

    private BucketObjectForEvent mappingUploadObjectToBucketObjectEvent(FileToUpload fileToUpload) {
        BucketObjectForEvent bucketObjectForEvent = new BucketObjectForEvent();
        bucketObjectForEvent.setBucketName( fileToUpload.getBucketName() );
        bucketObjectForEvent.setObjectName( fileToUpload.getUploadedFileName() );
        bucketObjectForEvent.setOwnerName( fileToUpload.getOwnerOfTheFile() );
        bucketObjectForEvent.setUserName( fileToUpload.getOwnerOfTheFile() );
        return bucketObjectForEvent;
    }

    // Have to implement distributed transactions in future

    public void uploadObjectToS3(List<FileToUpload> filesToUpload) {
        log.info( "inside uploadObjectToS3 " );
        try {
            List<Transfer.TransferState> s3uploadResult = new ArrayList<>();
            ListenableFuture<SendResult<String, String>> producerResult = lockTheGivenObjects( filesToUpload );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {
                filesToUpload.forEach( fileToUpload -> {
                    s3uploadResult.add( s3WriteService.uploadObject( fileToUpload ) ); // uploading tos3
                    //update local cache.
                    if (cacheManager.checkWhetherObjectExistInCache( fileToUpload.getUploadedFileName() )) {
                        CacheUpdateThread cacheUpdateThread = new CacheUpdateThread( fileToUpload );
                        cacheUpdateThread.thread.start();
                    }
                } );
            }
            if (s3uploadResult.contains( Transfer.TransferState.Completed )) {
                List<BucketObjectForEvent> uploadObjectEvents = filesToUpload.stream().map( this::mappingUploadObjectToBucketObjectEvent ).collect( Collectors.toList() );
                try {
                    kafkaTemplate.send( "AccessManagement", "uploadBucketObjects", jsonConverter.writeValueAsString( uploadObjectEvents ) );
                } catch (JsonProcessingException e) {
                    log.error( "Exception while publishing createEmptyFolder event " + e.getMessage() );
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error( " Exception while publishing lock to Kafka " + e.getCause() + e.getMessage() );
        }


    }
}
