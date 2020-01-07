package smarshare.coreservice.read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.read.dto.BucketMetadata;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.S3DownloadObject;
import smarshare.coreservice.read.model.S3DownloadedObject;
import smarshare.coreservice.read.model.filestructure.BASE64DecodedMultipartFile;
import smarshare.coreservice.read.service.helper.CacheInsertionThread;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ReadService {

    private S3ReadService s3ReadService;
    private ObjectMapper jsonConverter;
    private List<Bucket> bucketList = null;
    private LockServerAPIService lockServerAPIService;
    private AccessManagementAPIService accessManagementAPIService;
    private CacheManager cacheManager;

    @Autowired
    ReadService(S3ReadService s3ReadService, ObjectMapper jsonConverter, CacheManager cacheManager,
                LockServerAPIService lockServerAPIService, AccessManagementAPIService accessManagementAPIService) {
        this.s3ReadService = s3ReadService;
        this.jsonConverter = jsonConverter;
        this.lockServerAPIService = lockServerAPIService;
        this.accessManagementAPIService = accessManagementAPIService;
        this.cacheManager = cacheManager;
    }

    public List<Bucket> getBucketListFromS3() {
        log.info( "Inside getBucketListFromS3" );
        if (bucketList == null){
            bucketList = s3ReadService.listBuckets();
        }
        return bucketList;
    }

    // have to be used from controller
    public List<Bucket> getBucketListFromSpecificUser(String userName) {
        log.info( "Inside getBucketListFromSpecificUser" );
        List<Bucket> bucketsInS3 = getBucketListFromS3();
        List<Map<String, BucketMetadata>> bucketsMetadata = accessManagementAPIService.fetchAccessDetailsForBuckets( userName );
        for (Bucket eachBucket : bucketsInS3) {
            Optional<Map<String, BucketMetadata>> bucketMetadata = bucketsMetadata.stream().filter( stringBucketMetadataMap -> stringBucketMetadataMap.containsKey( eachBucket.getName() ) ).findFirst();
            bucketMetadata.ifPresent( stringBucketMetadataMap -> eachBucket.setBucketMetadata( stringBucketMetadataMap.get( eachBucket.getName() ) ) );
        }
        System.out.println( "bucketswithAccess ------>" + bucketsInS3 );
        return bucketsInS3;
    }


    public String getFilesAndFoldersListByUserAndBucket(String userName, String bucketName) {
        log.info( "Inside getFilesAndFoldersByUserAndBucket" );
        return s3ReadService.listObjectsWithMetadata( userName, bucketName );
    }

    private S3DownloadedObject getCachedObject(S3DownloadObject s3DownloadObject) {
        try {
            BASE64DecodedMultipartFile cachedS3DownloadedObject = cacheManager.getCachedObject( s3DownloadObject.getObjectName() );
            if (null != cachedS3DownloadedObject)
                return new S3DownloadedObject( s3DownloadObject, cachedS3DownloadedObject.getResource() );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public S3DownloadedObject downloadFile(S3DownloadObject s3DownloadObject) {
        log.info( "Inside downloadFile" );

        try {
            S3DownloadedObject cachedObject = getCachedObject( s3DownloadObject );
            if (null != cachedObject) return cachedObject;
            // have to confirm whether object name matches with name in lock server
            if (lockServerAPIService.getLockStatusForGivenObject( s3DownloadObject.getObjectName() )) {
                S3DownloadedObject s3DownloadedObject = s3ReadService.getObject( s3DownloadObject );
                CacheInsertionThread cacheInsertionThread = new CacheInsertionThread( s3DownloadObject, s3DownloadedObject );
                cacheInsertionThread.thread.start();
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

        try {
            if (!getLockStatusForTheObjectsToBeUploaded( objectsToBeDownloaded ).contains( Boolean.FALSE )) {
                List<S3DownloadedObject> downloadedObjects = new ArrayList<>();
                for (S3DownloadObject eachObjectToBeDownloaded : objectsToBeDownloaded) {
                    S3DownloadedObject cachedObject = getCachedObject( eachObjectToBeDownloaded );
                    if (null != cachedObject) downloadedObjects.add( cachedObject );
                    S3DownloadedObject s3DownloadedObject = s3ReadService.getObject( eachObjectToBeDownloaded );
                    CacheInsertionThread cacheInsertionThread = new CacheInsertionThread( eachObjectToBeDownloaded, s3DownloadedObject );
                    cacheInsertionThread.thread.start();
                    downloadedObjects.add( s3DownloadedObject );
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
