package smarshare.coreservice.write.service;

import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.transfer.Transfer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.write.dto.BucketObjectEvent;
import smarshare.coreservice.write.dto.DeleteObjectRequest;
import smarshare.coreservice.write.dto.DeleteObjectsRequest;
import smarshare.coreservice.write.helper.CacheDeleteThread;
import smarshare.coreservice.write.helper.CacheUpdateThread;
import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.model.lock.S3Object;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

//import smarshare.coreservice.write.helper.SagaOrchestratorThread;

@Slf4j
@Service
@Component
public class BucketObjectService {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private S3WriteService s3WriteService;
    private CacheManager cacheManager;


    @Qualifier("sagaOrchestratorTaskExecutor")
    private Executor sagaTaskExecutor;


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    BucketObjectService(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter, CacheManager cacheManager,
                        S3WriteService s3WriteService, Executor sagaTaskExecutor) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
        this.s3WriteService = s3WriteService;
        this.cacheManager = cacheManager;
        this.sagaTaskExecutor = sagaTaskExecutor;
    }


    public Boolean createEmptyFolder(UploadObject emptyFolder) {
        log.info( "Inside createEmptyFolder" );
        Boolean createObjectResult = s3WriteService.createObjectInBucket( emptyFolder );

        try {
            if (createObjectResult) {
                BucketObjectEvent bucketObjectForEvent = new BucketObjectEvent();
                bucketObjectForEvent.setBucketName( emptyFolder.getBucketName() );
                bucketObjectForEvent.setObjectName( emptyFolder.getObjectName() );
                bucketObjectForEvent.setOwnerName( emptyFolder.getOwner() );
                bucketObjectForEvent.setUserName( emptyFolder.getOwner() );
                System.out.println( Arrays.toString( Collections.singletonList( bucketObjectForEvent ).toArray() ) );
                System.out.println( Collections.singletonList( bucketObjectForEvent ) );
                kafkaTemplate.send( "BucketObjectAccessManagement", "emptyBucketObject", jsonConverter.writeValueAsString( Collections.singletonList( bucketObjectForEvent ) ) );
            }
        } catch (JsonProcessingException e) {
            log.error( "Exception while publishing createEmptyFolder event " + e.getMessage() );
        }
        return createObjectResult;
    }


    private void deleteObjectInCache(String objectName) {
        log.info( "Inside deleteObjectInCache" );
        if (cacheManager.checkWhetherObjectExistInCache( objectName )) {
            CacheDeleteThread cacheUpdateThread = new CacheDeleteThread( objectName, cacheManager );
            cacheUpdateThread.thread.start();
        }
    }

    private BucketObjectEvent mapBucketObjectToBucketObjectEvent(String objectName, String bucketName, String ownerName) {
        BucketObjectEvent bucketObjectDeleteEvent = new BucketObjectEvent();
        bucketObjectDeleteEvent.setBucketName( bucketName );
        bucketObjectDeleteEvent.setObjectName( objectName );
        bucketObjectDeleteEvent.setOwnerName( ownerName );
        return bucketObjectDeleteEvent;
    }

    public Boolean deleteObject(String objectName, String bucketName, String ownerName) {
        log.info( "Inside deleteFileInStorage" );
        try {
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock1", "object", jsonConverter.writeValueAsString( new S3Object( bucketName + "/" + objectName ) ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {
                if (s3WriteService.deleteObject( objectName, bucketName )) {
                    BucketObjectEvent bucketObjectDeleteEvent = mapBucketObjectToBucketObjectEvent( objectName, bucketName, ownerName );
                    kafkaTemplate.send( "BucketObjectAccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( Collections.singletonList( bucketObjectDeleteEvent ) ) );
                    deleteObjectInCache( bucketName + "/" + objectName );
                    return true;
                }
            }

        } catch (Exception exception) {
            log.error( " Exception while publishing lock to Kafka " + exception.getCause() + exception.getMessage() );
        }
        return false;
    }

    private List<DeleteObjectRequest> getObjectsByPrefix(String objectName, String bucketName) {
        return s3WriteService.listObjectsByPrefix( objectName, bucketName ).stream()
                .map( s3ObjectSummary -> {
                    DeleteObjectRequest deleteObjectsRequest = new DeleteObjectRequest();
                    deleteObjectsRequest.setObjectName( s3ObjectSummary.getKey() );
                    deleteObjectsRequest.setBucketName( bucketName );
                    deleteObjectsRequest.setOwnerName( s3ObjectSummary.getOwner().getDisplayName() );
                    return deleteObjectsRequest;
                } ).collect( Collectors.toList() );

    }

    public Boolean deleteFolderInStorage(DeleteObjectsRequest deleteObjectsRequest) {
        log.info( "Inside deleteFolderInStorage" );
        try {


            List<S3Object> objectsToBeLocked = deleteObjectsRequest.getFolderObjects().stream()
                    .map( deleteObjectRequest -> new S3Object( deleteObjectRequest.getBucketName() + "/" + deleteObjectRequest.getObjectName() ) )
                    .collect( Collectors.toList() );

            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock1", "objects", jsonConverter.writeValueAsString( objectsToBeLocked ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {

                List<String> objectNames = deleteObjectsRequest.getFolderObjects().stream()
                        .map( DeleteObjectRequest::getObjectName )
                        .collect( Collectors.toList() );
                DeleteObjectsResult deleteObjectsResult = Objects.requireNonNull( s3WriteService.deleteObjects( objectNames, deleteObjectsRequest.getBucketName() ) );
                log.info( deleteObjectsResult.getDeletedObjects().size() + " : Bucket Objects Deleted" );
                if (deleteObjectsResult.getDeletedObjects().size() == objectsToBeLocked.size()) {
                    List<BucketObjectEvent> bucketObjectsForDeleteEvent = deleteObjectsRequest.getFolderObjects().stream()
                            .map( deleteObjectRequest -> mapBucketObjectToBucketObjectEvent(
                                    deleteObjectRequest.getObjectName(),
                                    deleteObjectRequest.getBucketName(),
                                    deleteObjectRequest.getOwnerName() )
                            )
                            .collect( Collectors.toList() );
                    kafkaTemplate.send( "BucketObjectAccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( bucketObjectsForDeleteEvent ) );
                    deleteObjectsRequest.getFolderObjects().forEach( deleteObjectRequest -> deleteObjectInCache( deleteObjectRequest.getBucketName() + "/" + deleteObjectRequest.getObjectName() ) );
                    return true;
                }
            }
        } catch (Exception exception) {
            log.error( " Exception while publishing lock to Kafka " + exception.getCause() + exception.getMessage() );
        }
        return false;
    }

    public List<Boolean> deleteObjectsForSagaFromS3(List<UploadObject> objectsTobeDeleted) {

        log.info( "inside deleteObjectsForSagaFromS3 " );

        return objectsTobeDeleted.stream()
                .map( objectToDelete -> {
                    deleteObjectInCache( objectToDelete.getBucketName() + "/" + objectToDelete.getObjectName() );
                    return s3WriteService.deleteObject( objectToDelete.getObjectName(), objectToDelete.getBucketName() );
                } ).collect( Collectors.toList() );

    }


    public Transfer.TransferState uploadObjectToS3(UploadObject uploadObject) {
        return s3WriteService.uploadObject( uploadObject );
    }


    public Boolean uploadObjectsToS3(List<UploadObject> uploadObjects) {
        log.info( "inside uploadObjectToS3 " );

        try {
            if (s3WriteService.uploadObjects( uploadObjects )) {
                for (UploadObject uploadObject : uploadObjects) {
                    if (cacheManager.checkWhetherObjectExistInCache( uploadObject.getBucketName() + uploadObject.getObjectName() )) {
                        CacheUpdateThread cacheUpdateThread = new CacheUpdateThread( uploadObject );
                        cacheUpdateThread.thread.start();
                    }
                }
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error( "Exception while uploading  " + uploadObjects.toString() );
        }
        return Boolean.FALSE;
    }

}
