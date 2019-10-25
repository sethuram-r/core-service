package smarshare.coreservice.read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.S3DownloadObject;
import smarshare.coreservice.read.model.S3DownloadedObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReadService {

    private S3ReadService s3ReadService;
    private ObjectMapper jsonConverter;
    private List<Bucket> bucketList = null;
    private LockServerAPIService lockServerAPIService;

    @Autowired
    ReadService(S3ReadService s3ReadService, ObjectMapper jsonConverter, LockServerAPIService lockServerAPIService) {
        this.s3ReadService = s3ReadService;
        this.jsonConverter = jsonConverter;
        this.lockServerAPIService = lockServerAPIService;
    }

    public List<Bucket> getBucketListFromS3() {
        log.info( "Inside getBucketListFromS3" );
        if (bucketList == null){
            bucketList = s3ReadService.listBuckets();
        }
        return bucketList;
    }

    public List<Bucket> getFilesAndFoldersListByUserAndBucket(String userName, String bucketName) {
        log.info( "Inside getFilesAndFoldersByUserAndBucket" );
        s3ReadService.listObjects( userName, bucketName );
        return null;
    }

    public S3DownloadedObject downloadFile(S3DownloadObject s3DownloadObject) {
        log.info( "Inside downloadFile" );
        /* have to implement cache logic */
        try {
            // have to confirm whether object name matches with name in lock server
            if (lockServerAPIService.getLockStatusForGivenObject( s3DownloadObject.getObjectName() )) {
                return s3ReadService.getObject( s3DownloadObject );
            }
        } catch (Exception e) {
            log.error( "Exception while downloading the file" + e.getMessage() + e.getCause() );
        }
        return null;
    }

    private List<Boolean> getLockStatusForTheObjectsToBeUploaded(List<S3DownloadObject> objectsToBeDownloaded) {
        log.info( "Inside getLockStatusForTheObjectsToBeUploaded" );
        List<String> objectNames = new ArrayList<>();
        if (!objectsToBeDownloaded.isEmpty()) {
            objectsToBeDownloaded.forEach( s3DownloadObject -> {
                objectNames.add( s3DownloadObject.getObjectName() );
            } );
        }
        return lockServerAPIService.getLockStatusForGivenObjects( objectNames );
    }

    public List<S3DownloadedObject> downloadFolder(List<S3DownloadObject> objectsToBeDownloaded) {
        log.info( "Inside downloadFolder" );
        /* have to implement cache logic */
        try {
            if (!getLockStatusForTheObjectsToBeUploaded( objectsToBeDownloaded ).contains( Boolean.FALSE )) {
                List<S3DownloadedObject> downloadedObjects = new ArrayList<>();
                for (S3DownloadObject eachObjectToBeDownloaded : objectsToBeDownloaded) {
                    downloadedObjects.add( s3ReadService.getObject( eachObjectToBeDownloaded ) );
                }
                return downloadedObjects;
            }
        } catch (Exception e) {
            log.error( "Exception while downloading the folder" + e.getMessage() + e.getCause() );
        }

        return null;
    }

    //    @KafkaListener(groupId="readConsumer",topics = "read")
    public void consume(String bucketToBeUpdatedOrDeletedInInternalCache, ConsumerRecord record) throws IOException {
        System.out.println( "bucketToBeUpdatedInInternalCache------------->" + bucketToBeUpdatedOrDeletedInInternalCache );
        System.out.println( "record--------->" + record );

        if (record.key() == ("add")) {
            log.info( "Consumed Cache add Event" );
            Bucket bucketToBeAddedInCache = jsonConverter.readValue( bucketToBeUpdatedOrDeletedInInternalCache, Bucket.class );
            System.out.println( "result----file----->" + bucketToBeAddedInCache );
            if (!bucketList.isEmpty()) {
                bucketList.add( bucketToBeAddedInCache );
                log.info( "Bucket has been added in the cache" );
            }

        }
        if (record.key() == ("delete")) {
            log.info( "Consumed Cache delete Event" );
            Bucket bucketToBeDeletedInCache = jsonConverter.readValue( bucketToBeUpdatedOrDeletedInInternalCache, Bucket.class );
            System.out.println( "result----file----->" + bucketToBeDeletedInCache );
            if (!bucketList.isEmpty()) {
                bucketList.remove( bucketToBeDeletedInCache );
                log.info( "Bucket has been deleted from the cache" );
            }

        }
    }
}
