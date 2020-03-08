package smarshare.coreservice.write.service;

import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.transfer.Transfer;
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
import smarshare.coreservice.write.helper.SagaOrchestratorThread;
import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.model.lock.S3Object;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class BucketObjectService {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private S3WriteService s3WriteService;
    private CacheManager cacheManager;


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
            if (createObjectResult) {
                BucketObjectEvent bucketObjectForEvent = new BucketObjectEvent();
                bucketObjectForEvent.setBucketName( emptyFolder.getBucketName() );
                bucketObjectForEvent.setObjectName( emptyFolder.getObjectName() );
                bucketObjectForEvent.setOwnerName( emptyFolder.getOwner() );
                bucketObjectForEvent.setUserName( emptyFolder.getOwner() );
                System.out.println( Arrays.toString( Collections.singletonList( bucketObjectForEvent ).toArray() ) );
                System.out.println( Collections.singletonList( bucketObjectForEvent ) );
                kafkaTemplate.send( "AccessManagement", "emptyBucketObject", jsonConverter.writeValueAsString( Collections.singletonList( bucketObjectForEvent ) ) );
            }
        } catch (JsonProcessingException e) {
            log.error( "Exception while publishing createEmptyFolder event " + e.getMessage() );
        }
        return createObjectResult;
    }


    private void deleteObjectInCache(String objectName) {
        log.info( "Inside deleteObjectInCache" );
        if (cacheManager.checkWhetherObjectExistInCache( objectName )) {
            CacheDeleteThread cacheUpdateThread = new CacheDeleteThread( objectName );
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
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "object", jsonConverter.writeValueAsString( new S3Object( bucketName + "/" + objectName ) ) );
            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {
                if (s3WriteService.deleteObject( objectName, bucketName )) {
                    BucketObjectEvent bucketObjectDeleteEvent = mapBucketObjectToBucketObjectEvent( objectName, bucketName, ownerName );
                    kafkaTemplate.send( "AccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( Collections.singletonList( bucketObjectDeleteEvent ) ) );
                    deleteObjectInCache( bucketName + "/" + objectName );
                    return true;
                }
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

            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "objects", jsonConverter.writeValueAsString( objectsToBeLocked ) );
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
                    kafkaTemplate.send( "AccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( bucketObjectsForDeleteEvent ) );
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


    public Boolean UploadObjectThroughSaga(List<UploadObject> filesToUpload) {
        log.info( "inside UploadObjectThroughSaga " );
        try {
            SagaOrchestratorThread sagaOrchestratorThread = new SagaOrchestratorThread( filesToUpload );
            sagaOrchestratorThread.thread.start();
            return true;
        } catch (Exception e) {
            log.error( "Exception while starting Saga Upload Orchestrator Thread " + e );
        }
        return false;
    }


//    private ListenableFuture<SendResult<String, String>> lockTheGivenObjects(List<FileToUpload> filesToUpload) {
//        List<S3Object> objectsToBeLocked = new ArrayList<>();
//        try {
//            filesToUpload.forEach( fileToUpload -> {
//                objectsToBeLocked.add( new S3Object( fileToUpload.getUploadedFileName(), Boolean.TRUE ) );
//            } );
//            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "objects", jsonConverter.writeValueAsString( new S3ObjectsWrapper( objectsToBeLocked ) ) );
//            return producerResult;
//        } catch (JsonProcessingException e) {
//            log.error( " Exception while publishing lock to Kafka " + e.getCause() + e.getMessage() );
//        }
//        return null;
//    }

//    private BucketObjectForEvent mappingUploadObjectToBucketObjectEvent(FileToUpload fileToUpload) {
//        BucketObjectForEvent bucketObjectForEvent = new BucketObjectForEvent();
//        bucketObjectForEvent.setBucketName( fileToUpload.getBucketName() );
//        bucketObjectForEvent.setObjectName( fileToUpload.getUploadedFileName() );
//        bucketObjectForEvent.setOwnerName( fileToUpload.getOwnerOfTheFile() );
//        bucketObjectForEvent.setUserName( fileToUpload.getOwnerOfTheFile() );
//        return bucketObjectForEvent;
//    }


//    // Have to implement distributed transactions in future
//
//    public void uploadObjectToS3(List<FileToUpload> filesToUpload) {
//        log.info( "inside uploadObjectToS3 " );
////        try {
//            List<Transfer.TransferState> s3uploadResult = new ArrayList<>();
////            ListenableFuture<SendResult<String, String>> producerResult = lockTheGivenObjects( filesToUpload );
////            if (!producerResult.get().getRecordMetadata().toString().isEmpty()) {
//                filesToUpload.forEach( fileToUpload -> {
//                    s3uploadResult.add( s3WriteService.uploadObject( fileToUpload ) ); // uploading tos3
//                    //update local cache.
//                    if (cacheManager.checkWhetherObjectExistInCache( fileToUpload.getUploadedFileName() )) {
//                        CacheUpdateThread cacheUpdateThread = new CacheUpdateThread( fileToUpload );
//                        cacheUpdateThread.thread.start();
//                    }
//                } );
////            }
//            if (s3uploadResult.contains( Transfer.TransferState.Completed )) {
//                List<BucketObjectForEvent> uploadObjectEvents = filesToUpload.stream().map( this::mappingUploadObjectToBucketObjectEvent ).collect( Collectors.toList() );
//                try {
//                    kafkaTemplate.send( "AccessManagement", "uploadBucketObjects", jsonConverter.writeValueAsString( uploadObjectEvents ) );
//                } catch (JsonProcessingException e) {
//                    log.error( "Exception while publishing createEmptyFolder event " + e.getMessage() );
//                }
//            }
////        } catch (InterruptedException | ExecutionException e) {
////            log.error( " Exception while publishing lock to Kafka " + e.getCause() + e.getMessage() );
////        }
//
//
//    }
}
