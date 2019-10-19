package smarshare.coreservice.write.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.write.exception.BucketExistException;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.Status;

@Slf4j
@Service
public class S3Service {

    private AmazonS3 amazonS3Client;
    private Status status;

    @Autowired
    S3Service(AmazonS3 amazonS3Client, Status status) {
        this.amazonS3Client = amazonS3Client;
        this.status = status;
    }

    public Status createBucket(Bucket bucket) {

        log.info( "Inside createBucket in S3Service" );

        if (amazonS3Client.doesBucketExistV2( bucket.getName() )) {
            throw new BucketExistException( bucket.getName() + " already exists!" );
        } else {
            try {
                amazonS3Client.createBucket( bucket.getName() );
                status.setMessage( "Success" );
                return status;
            } catch (AmazonS3Exception e) {
                log.error( e.getErrorMessage() );
                status.setMessage( "Failed" );
                return status;
            }
        }
    }
}
