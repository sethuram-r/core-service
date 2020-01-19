package smarshare.coreservice.write.sagas.upload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.write.dto.BucketObjectForEvent;
import smarshare.coreservice.write.helper.Mapper;
import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.model.lock.S3Object;
import smarshare.coreservice.write.model.lock.S3ObjectsWrapper;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FailureStateOperations {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private List<FileToUpload> consumedAccessRecord;
    private List<FileToUpload> consumedUnLockResultRecord;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    FailureStateOperations(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;

    }

    boolean unLockEventToKafka(List<FileToUpload> filesToUpload) {
        log.info( "Inside unLockEventToKafka" );
        try {
            List<S3Object> objectsToBeUnLocked = filesToUpload.stream().map( fileToUpload -> new S3Object( fileToUpload.getUploadedFileName(), Boolean.FALSE ) )
                    .collect( Collectors.toList() );
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "objects", jsonConverter.writeValueAsString( new S3ObjectsWrapper( objectsToBeUnLocked ) ) );
            if (producerResult.isDone()) return producerResult.get().getRecordMetadata().toString().isEmpty();

        } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
            log.error( " Exception while publishing lock to Kafka " + e.getCause() + e.getMessage() );
        }
        return false;
    }

    public List<FileToUpload> getConsumedAccessRecord(List<FileToUpload> filesToUpload) {
        return this.consumedAccessRecord.size() == filesToUpload.size() ? consumedAccessRecord : null;
    }

    public List<FileToUpload> getConsumedUnLockResultRecord(List<FileToUpload> filesToUpload) {
        return this.consumedUnLockResultRecord.size() == filesToUpload.size() ? consumedUnLockResultRecord : null;
    }

//    private List<FileToUpload> createInputParameterAsRequired(List<String> folderObjects, String bucketName){
//        return folderObjects.stream()
//                .map(  folderObject ->new FileToUpload().setBucketName( bucketName ).setUploadedFileName( folderObject ))
//                .collect( Collectors.toList() );
//    }

    boolean accessManagementServiceDeleteEntryEventToKafka(List<FileToUpload> objectsToDelete) {
        log.info( "Inside accessManagementServiceDeleteEntryEventToKafka" );

        List<BucketObjectForEvent> bucketObjectsForDeleteEvent = objectsToDelete.stream()
                .map( Mapper::mappingUploadObjectToBucketObjectEvent )
                .collect( Collectors.toList() );
        try {
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "AccessManagement", "deleteBucketObjects", jsonConverter.writeValueAsString( bucketObjectsForDeleteEvent ) );
            if (producerResult.isDone()) return producerResult.get().getRecordMetadata().toString().isEmpty();
        } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
            log.error( "Exception while publishing accessManagementServiceDeleteEntryEventToKafka event " + e.getMessage() );
        }
        return false;
    }


    @KafkaListener(groupId = "sagaConsumer", topics = "sagaConsumer")
    public Object accessRecordDeleteResultConsumer(ConsumerRecord record) {
        log.info( "Inside accessRecordDeleteResultConsumer" );
        try {
            if (record.key() == ("accessDeleteResult")) {
                this.consumedAccessRecord = (List<FileToUpload>) record.value();
            }
            if (record.key() == ("UnLockResult")) {
                this.consumedUnLockResultRecord = (List<FileToUpload>) record.value();
            }
        } catch (Exception e) {
            log.error( "Exception while consuming accessRecordDeleteResultConsumer event " + e.getMessage() );
        }

        return null;
    }


}
