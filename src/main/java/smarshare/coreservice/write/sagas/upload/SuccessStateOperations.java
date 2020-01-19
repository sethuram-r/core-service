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
public class SuccessStateOperations {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private List<FileToUpload> consumedAccessRecord;
    private List<FileToUpload> consumedLockResultRecord;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    SuccessStateOperations(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;

    }

    boolean lockEventToKafka(List<FileToUpload> filesToUpload) {
        log.info( "Inside lockEventToKafka" );
        try {
            List<S3Object> objectsToBeLocked = filesToUpload.stream().map( fileToUpload -> new S3Object( fileToUpload.getUploadedFileName(), Boolean.TRUE ) )
                    .collect( Collectors.toList() );
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "lock", "objects", jsonConverter.writeValueAsString( new S3ObjectsWrapper( objectsToBeLocked ) ) );
            if (producerResult.isDone()) return producerResult.get().getRecordMetadata().toString().isEmpty();

        } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
            log.error( " Exception while publishing lock to Kafka " + e.getCause() + e.getMessage() );
        }
        return false;
    }

    boolean accessManagementServiceCreateEntryEventToKafka(List<FileToUpload> filesToUpload) {
        log.info( "Inside accessManagementServiceCreateEntryEventToKafka" );
        List<BucketObjectForEvent> uploadObjectEvents = filesToUpload.stream()
                .map( Mapper::mappingUploadObjectToBucketObjectEvent ).collect( Collectors.toList() );
        try {
            ListenableFuture<SendResult<String, String>> producerResult = kafkaTemplate.send( "AccessManagement", "uploadBucketObjects", jsonConverter.writeValueAsString( uploadObjectEvents ) );
            if (producerResult.isDone()) return producerResult.get().getRecordMetadata().toString().isEmpty();
        } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
            log.error( "Exception while publishing accessManagementServiceCreateEntryEventToKafka event " + e.getMessage() );
        }
        return false;
    }

    public List<FileToUpload> getConsumedAccessRecord(List<FileToUpload> filesToUpload) {
        return this.consumedAccessRecord.size() == filesToUpload.size() ? consumedAccessRecord : null;
    }

    public List<FileToUpload> getConsumedLockResultRecord(List<FileToUpload> filesToUpload) {
        return this.consumedLockResultRecord.size() == filesToUpload.size() ? consumedLockResultRecord : null;
    }

    @KafkaListener(groupId = "sagaConsumer", topics = "sagaConsumer")
    public Object accessRecordCreateResultConsumer(ConsumerRecord record) {
        log.info( "Inside accessManagementServiceCreateEntryEventToKafka" );
        try {
            if (record.key() == ("accessCreateResult")) {
                this.consumedAccessRecord = (List<FileToUpload>) record.value();
            }
            if (record.key() == ("lockResult")) {
                this.consumedLockResultRecord = (List<FileToUpload>) record.value();
            }
        } catch (Exception e) {
            log.error( "Exception while consuming accessRecordCreateResult event " + e.getMessage() );
        }

        return null;
    }


}



