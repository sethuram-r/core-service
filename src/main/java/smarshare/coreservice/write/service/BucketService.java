package smarshare.coreservice.write.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import smarshare.coreservice.write.model.Bucket;

@Slf4j
@Service
public class BucketService {

    private S3WriteService s3WriteService;
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public BucketService(S3WriteService s3WriteService, KafkaTemplate<String, String> kafkaTemplate) {
        this.s3WriteService = s3WriteService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Boolean createBucket(Bucket bucket) {
        log.info( "Inside createBucketInStorage" );
        Boolean isBucketCreated = s3WriteService.createBucket( bucket );
        if (isBucketCreated) {
            try {
                ListenableFuture<SendResult<String, String>> isAccessInfoEventSent = kafkaTemplate.send( "AccessManagement", "createBucket", bucket.getBucketName() );
                ListenableFuture<SendResult<String, String>> isUpdatingTheCacheEventForReadServerSent = kafkaTemplate.send( "read", "add", bucket.getBucketName() );
                if (!(isUpdatingTheCacheEventForReadServerSent.get().getRecordMetadata().toString().isEmpty()
                        && isAccessInfoEventSent.get().getRecordMetadata().toString().isEmpty()))
                    return true;
            } catch (Exception exception) {
                log.error( " Exception while publishing user to Kafka " + exception.getCause() + exception.getMessage() );
                return false;
            }
        }
        return isBucketCreated;
    }

    public Boolean deleteBucket(String bucketName) {
        log.info( "Inside deleteBucketInStorage" );
        Boolean deleteBucketStatus = s3WriteService.deleteBucket( bucketName );
        if (deleteBucketStatus) {
            try {
                ListenableFuture<SendResult<String, String>> isAccessInfoEventSent = kafkaTemplate.send( "AccessManagement", "deleteBucket", bucketName );
                ListenableFuture<SendResult<String, String>> isUpdatingTheCacheEventForReadServerSent = kafkaTemplate.send( "read", "delete", bucketName );
                if (!(isUpdatingTheCacheEventForReadServerSent.get().getRecordMetadata().toString().isEmpty()
                        && isAccessInfoEventSent.get().getRecordMetadata().toString().isEmpty()))
                    return true;
            } catch (Exception exception) {
                log.error( " Exception while publishing user to Kafka " + exception.getCause() + exception.getMessage() );
                return false;
            }
        }
        return deleteBucketStatus;
    }

}
