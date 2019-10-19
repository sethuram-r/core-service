package smarshare.coreservice.write.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.Status;

@Slf4j
@Service
public class WriteService {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectWriter jsonConverter;
    private S3Service s3Service;


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    WriteService(KafkaTemplate<String, String> kafkaTemplate, ObjectWriter jsonConverter, S3Service s3Service) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
        this.s3Service = s3Service;
    }

    public Status createBucketInStorage(Bucket bucket) {
        log.info( "Inside createBucketInStorage" );
        Status createBucketStatus = s3Service.createBucket( bucket );
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
        Status deleteBucketStatus = s3Service.deleteBucket( bucket );
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
}
