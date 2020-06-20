package smarshare.coreservice.write.service;

import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import smarshare.coreservice.write.sagas.constants.KafkaConstants;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@Component
public class BucketObjectService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectWriter jsonConverter;
    private final S3WriteService s3WriteService;
    private final CacheManager cacheManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    BucketObjectService(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter, CacheManager cacheManager,
                        S3WriteService s3WriteService) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
        this.s3WriteService = s3WriteService;
        this.cacheManager = cacheManager;
    }


    public Boolean createEmptyFolder(UploadObject emptyFolder) {
        log.info( "Inside createEmptyFolder" );
        Boolean createObjectResult = s3WriteService.createObjectInBucket( emptyFolder );

        try {
            if (createObjectResult.equals( Boolean.TRUE )) {
                BucketObjectEvent bucketObjectForEvent = new BucketObjectEvent();
                bucketObjectForEvent.setBucketName( emptyFolder.getBucketName() );
                bucketObjectForEvent.setObjectName( emptyFolder.getObjectName() );
                bucketObjectForEvent.setOwnerName( emptyFolder.getOwner() );
                bucketObjectForEvent.setOwnerId( emptyFolder.getOwnerId() );
                bucketObjectForEvent.setUserId( emptyFolder.getOwnerId() );
                bucketObjectForEvent.setUserName( emptyFolder.getOwner() );

                String stringConverted = jsonConverter.writeValueAsString( Collections.singletonList( bucketObjectForEvent ) );
                kafkaTemplate.send( "BucketObjectAccessManagement", "emptyBucketObject", stringConverted );
            }
        } catch (JsonProcessingException e) {
            log.error( "Exception while publishing createEmptyFolder event " + e.getMessage() );
        }
        return createObjectResult;
    }


    private void deleteObjectInCache(String objectName) {
        log.info( "Inside deleteObjectInCache" );
        if (cacheManager.checkWhetherObjectExistInCache( objectName ).equals( Boolean.TRUE )) {
            CacheDeleteThread cacheUpdateThread = new CacheDeleteThread( objectName, cacheManager );
            cacheUpdateThread.thread.start();
        }
    }

    private BucketObjectEvent mapBucketObjectToBucketObjectEvent(String objectName, String bucketName, int ownerId) {
        BucketObjectEvent bucketObjectDeleteEvent = new BucketObjectEvent();
        bucketObjectDeleteEvent.setBucketName( bucketName );
        bucketObjectDeleteEvent.setObjectName( objectName );
        bucketObjectDeleteEvent.setOwnerId( ownerId );
        return bucketObjectDeleteEvent;
    }

    public Boolean deleteObject(String objectName, String bucketName, int ownerId) {
        log.info( "Inside deleteObject" );
        try {
            final String lockEventObjects = jsonConverter.writeValueAsString( new S3Object( bucketName + "/" + objectName ) );
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( KafkaConstants.LOCK_TOPIC.valueOf(), KafkaConstants.LOCK_KEY.valueOf(), lockEventObjects );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty() && s3WriteService.deleteObject( objectName, bucketName ).equals( Boolean.TRUE )) {

                BucketObjectEvent bucketObjectDeleteEvent = mapBucketObjectToBucketObjectEvent( objectName, bucketName, ownerId );
                kafkaTemplate.send( "BucketObjectAccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( Collections.singletonList( bucketObjectDeleteEvent ) ) );
                deleteObjectInCache( bucketName + "/" + objectName );
                kafkaTemplate.send( KafkaConstants.LOCK_TOPIC.valueOf(), KafkaConstants.UN_LOCK_KEY.valueOf(), lockEventObjects );
                return true;

            }

        } catch (Exception exception) {
            log.error( " Exception while publishing lock to Kafka " + exception.getCause() + exception.getMessage() );
        }
        return false;
    }


    public Boolean deleteFolderInStorage(DeleteObjectsRequest deleteObjectsRequest) {
        log.info( "Inside deleteFolderInStorage" );
        try {


            List<S3Object> objectsToBeLocked = deleteObjectsRequest.getFolderObjects().stream()
                    .map( deleteObjectRequest -> new S3Object( deleteObjectRequest.getBucketName() + "/" + deleteObjectRequest.getObjectName() ) )
                    .collect( Collectors.toList() );
            final String lockEventObjects = jsonConverter.writeValueAsString( objectsToBeLocked );
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( KafkaConstants.LOCK_TOPIC.valueOf(), KafkaConstants.LOCK_KEY.valueOf(), lockEventObjects );
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
                                    deleteObjectRequest.getOwnerId() )
                            )
                            .collect( Collectors.toList() );

                    kafkaTemplate.send( "BucketObjectAccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( bucketObjectsForDeleteEvent ) );
                    deleteObjectsRequest.getFolderObjects().forEach( deleteObjectRequest -> deleteObjectInCache( deleteObjectRequest.getBucketName() + "/" + deleteObjectRequest.getObjectName() ) );
                    kafkaTemplate.send( KafkaConstants.LOCK_TOPIC.valueOf(), KafkaConstants.UN_LOCK_KEY.valueOf(), lockEventObjects );
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



    public Boolean uploadObjectsToS3(List<UploadObject> uploadObjects) {
        log.info( "inside uploadObjectToS3 " );

        try {
            if (s3WriteService.uploadObjects( uploadObjects ).equals( Boolean.TRUE )) {
                for (UploadObject uploadObject : uploadObjects) {
                    if (cacheManager.checkWhetherObjectExistInCache( uploadObject.getBucketName() + uploadObject.getObjectName() ).equals( Boolean.TRUE )) {
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
