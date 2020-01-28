package smarshare.coreservice.write.sagas.upload;


import com.amazonaws.services.s3.transfer.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.write.dto.BucketObjectForEvent;
import smarshare.coreservice.write.helper.Mapper;
import smarshare.coreservice.write.model.Status;
import smarshare.coreservice.write.model.lock.S3Object;
import smarshare.coreservice.write.sagas.constants.KafkaConstants;
import smarshare.coreservice.write.sagas.dto.SagaEventAccessManagementServiceWrapper;
import smarshare.coreservice.write.sagas.dto.SagaEventLockWrapper;
import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;
import smarshare.coreservice.write.service.WriteService;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UploadStateTasks {

    private KafkaTemplate<String, SagaEventLockWrapper> kafkaTemplateForLockServer;
    private KafkaTemplate<String, SagaEventAccessManagementServiceWrapper> kafkaTemplateForAccessManagementServer;
    private KafkaConsumer<String, SagaEventLockWrapper> kafkaLockConsumer;
    private KafkaConsumer<String, SagaEventAccessManagementServiceWrapper> kafkaAccessManagementConsumer;
    private WriteService writeService;


    @Autowired
    UploadStateTasks(KafkaTemplate<String, SagaEventLockWrapper> kafkaTemplateForLockServer,
                     KafkaTemplate<String, SagaEventAccessManagementServiceWrapper> kafkaTemplateForAccessManagementServer,
                     KafkaConsumer<String, SagaEventLockWrapper> kafkaLockConsumer,
                     KafkaConsumer<String, SagaEventAccessManagementServiceWrapper> kafkaAccessManagementConsumer,
                     WriteService writeService) {
        this.kafkaTemplateForLockServer = kafkaTemplateForLockServer;
        this.kafkaTemplateForAccessManagementServer = kafkaTemplateForAccessManagementServer;
        this.kafkaLockConsumer = kafkaLockConsumer;
        this.kafkaAccessManagementConsumer = kafkaAccessManagementConsumer;
        this.writeService = writeService;
    }


    /// Have to implement kafka producer in lock server for kafka result of state machines


    private Boolean sendEventToLockServer(SagaEventLockWrapper objectsToBeLocked) {

        log.info( "Inside sendEventToLockServer" );

        try {
            ListenableFuture<SendResult<String, SagaEventLockWrapper>> kafkaProducerFutureObject = kafkaTemplateForLockServer.send( KafkaConstants.SAGA_LOCK_TOPIC.valueOf(), KafkaConstants.LOCK.valueOf(), objectsToBeLocked );
            SendResult<String, SagaEventLockWrapper> producerResult = kafkaProducerFutureObject.get( 10, TimeUnit.SECONDS );
            return (null != producerResult.getRecordMetadata()) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception e) {
            log.error( "Exception in sendingEventToLockServer In " + this.getClass().getSimpleName() + " " + e.getMessage() + " " + e.getCause() );
        }
        return Boolean.FALSE;
    }


    private Boolean sendEventToAccessManagementServer(SagaEventAccessManagementServiceWrapper bucketObjectForEvents, String key) {
        log.info( "Inside sendEventToAccessManagementServer" );
        try {
            ListenableFuture<SendResult<String, SagaEventAccessManagementServiceWrapper>> kafkaProducerFutureObject = kafkaTemplateForAccessManagementServer.send( KafkaConstants.SAGA_ACCESS_TOPIC.valueOf(), key, bucketObjectForEvents );
            SendResult<String, SagaEventAccessManagementServiceWrapper> producerResult = kafkaProducerFutureObject.get( 10, TimeUnit.SECONDS );
            return (null != producerResult.getRecordMetadata()) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception e) {
            log.error( "Exception in sendingEventToLockServer In " + this.getClass().getSimpleName() + " " + e.getMessage() + " " + e.getCause() );
        }
        return Boolean.FALSE;
    }


    // Issue to address in lock server dto from saga and lock server doesn't match

    boolean lockEventToKafka(SagaEventWrapper objectsToBeLocked) {
        log.info( "Inside lockEventToKafka" );
        List<S3Object> objectsToBeLockedAsS3Object = objectsToBeLocked.getObjects().stream()
                .map( fileToUpload -> new S3Object( fileToUpload.getUploadedFileName(), Boolean.TRUE ) )
                .collect( Collectors.toList() );
        return sendEventToLockServer( new SagaEventLockWrapper( objectsToBeLocked.getEventId(), objectsToBeLockedAsS3Object ) );
    }


    boolean unLockEventToKafka(SagaEventWrapper objectsToBeUnLocked) {
        log.info( "Inside unLockEventToKafka" );

        List<S3Object> objectsToBeUnLockedAsS3Object = objectsToBeUnLocked.getObjects().stream()
                .map( fileToUpload -> new S3Object( fileToUpload.getUploadedFileName(), Boolean.FALSE ) )
                .collect( Collectors.toList() );
        return sendEventToLockServer( new SagaEventLockWrapper( objectsToBeUnLocked.getEventId(), objectsToBeUnLockedAsS3Object ) );
    }


    boolean accessManagementServiceCreateEntryEventToKafka(SagaEventWrapper objectsToCreateAccessDetails) {
        log.info( "Inside accessManagementServiceCreateEntryEventToKafka" );
        List<BucketObjectForEvent> uploadObjectEvents = objectsToCreateAccessDetails.getObjects().stream()
                .map( Mapper::mappingUploadObjectToBucketObjectEvent ).collect( Collectors.toList() );
        return sendEventToAccessManagementServer( new SagaEventAccessManagementServiceWrapper( objectsToCreateAccessDetails.getEventId(), uploadObjectEvents ), KafkaConstants.CREATE.valueOf() );
    }


    boolean accessManagementServiceDeleteEntryEventToKafka(SagaEventWrapper objectsToDeleteAccessDetails) {

        log.info( "Inside accessManagementServiceCreateEntryEventToKafka" );
        List<BucketObjectForEvent> uploadObjectEvents = objectsToDeleteAccessDetails.getObjects().stream()
                .map( Mapper::mappingUploadObjectToBucketObjectEvent ).collect( Collectors.toList() );
        return sendEventToAccessManagementServer( new SagaEventAccessManagementServiceWrapper( objectsToDeleteAccessDetails.getEventId(), uploadObjectEvents ), KafkaConstants.DELETE.valueOf() );
    }


    private boolean consumeLockServerEvents(SagaEventWrapper objectToBeConsumed, String key) {
        log.info( "Inside consumeLockServerEvents" );

        try {
            kafkaLockConsumer.subscribe( Collections.singletonList( KafkaConstants.SAGA_LOCK_TOPIC.valueOf() ) );
            while (true) {
                ConsumerRecords<String, SagaEventLockWrapper> consumedRecords = kafkaLockConsumer.poll( Duration.ofMillis( 15 ) );
                if (!consumedRecords.isEmpty()) {
                    for (ConsumerRecord<String, SagaEventLockWrapper> record : consumedRecords)
                        if (record.key().equals( key ) && record.value().getEventId().equals( objectToBeConsumed.getEventId() ))
                            return true;
                } else {
                    log.info( "Waiting for LockEvents" );
                }
            }

        } catch (Exception e) {
            System.out.println( "Exception in consumeLockServerEvents" + " " + e.getMessage() + " " + e.getCause() );

        } finally {
            kafkaLockConsumer.close();
        }
        return false;

    }

    private boolean consumeAccessManagementServerEvents(SagaEventWrapper objectToBeConsumed, String key) {
        log.info( "Inside consumeAccessManagementServerEvents" );
        try {
            kafkaAccessManagementConsumer.subscribe( Collections.singletonList( KafkaConstants.SAGA_ACCESS_TOPIC.valueOf() ) );
            while (true) {
                ConsumerRecords<String, SagaEventAccessManagementServiceWrapper> consumedRecords = kafkaAccessManagementConsumer.poll( Duration.ofMillis( 15 ) );
                if (!consumedRecords.isEmpty()) {
                    for (ConsumerRecord<String, SagaEventAccessManagementServiceWrapper> record : consumedRecords)
                        if (record.key().equals( key ) && record.value().getEventId().equals( objectToBeConsumed.getEventId() ))
                            return true;
                } else {
                    log.info( "Waiting for accessManagementEvents ..." );
                }
            }

        } catch (Exception e) {
            System.out.println( "Exception in consumeAccessManagementServerEvents" + " " + e.getMessage() + " " + e.getCause() );

        } finally {
            kafkaLockConsumer.close();
        }
        return false;

    }

    boolean consumeLockEventsFromLockServer(SagaEventWrapper objectToBeConsumed) {
        return consumeLockServerEvents( objectToBeConsumed, KafkaConstants.LOCK.valueOf() );
    }

    boolean consumeUnLockEventsFromLockServer(SagaEventWrapper objectToBeConsumed) {
        return consumeLockServerEvents( objectToBeConsumed, KafkaConstants.UN_LOCK.valueOf() );
    }

    boolean consumeCreateEventsFromAccessManagementServer(SagaEventWrapper objectToBeConsumed) {
        return consumeAccessManagementServerEvents( objectToBeConsumed, KafkaConstants.CREATE.valueOf() );
    }

    boolean consumeDeleteEventsFromAccessManagementServer(SagaEventWrapper objectToBeConsumed) {
        return consumeAccessManagementServerEvents( objectToBeConsumed, KafkaConstants.DELETE.valueOf() );
    }

    boolean uploadToS3AndRefreshCache(SagaEventWrapper objectToBeConsumed) {

        log.info( "Inside uploadToS3AndRefreshCache" );
        try {
            List<Transfer.TransferState> uploadedResult = writeService.uploadObjectToS3( objectToBeConsumed.getObjects() );
            return uploadedResult.stream().allMatch( transferState -> transferState.equals( Transfer.TransferState.Completed ) );
        } catch (Exception e) {
            log.error( "Exception in uploadToS3AndRefreshCache" + " " + e.getMessage() );
        }
        return false;
    }

    boolean deleteS3UploadAndCache(SagaEventWrapper objectToBeConsumed) {

        log.info( "Inside deleteS3UploadAndCache" );
        try {
            List<Status> deletionResult = writeService.deleteObjectsForSagaFromS3( objectToBeConsumed.getObjects() );
            return deletionResult.stream().allMatch( status -> status.getMessage().equals( "Success" ) );
        } catch (Exception e) {
            log.error( "Exception in uploadToS3AndRefreshCache" + " " + e.getMessage() );
        }
        return false;
    }


}