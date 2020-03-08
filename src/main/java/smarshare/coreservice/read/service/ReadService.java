package smarshare.coreservice.read.service;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.cache.model.DownloadedCacheObject;
import smarshare.coreservice.cache.model.FileToBeCached;
import smarshare.coreservice.read.dto.*;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.filestructure.BASE64DecodedMultipartFile;
import smarshare.coreservice.read.service.helper.CacheInsertionThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private List<Bucket> getBucketListFromS3() {
        log.info( "Inside getBucketListFromS3" );
        if (bucketList == null){
            return s3ReadService.listBuckets();
        }
        return bucketList;
    }

    public List<Bucket> getBucketsByUserName(String userName) {
        log.info( "Inside getBucketListFromSpecificUser" );
        List<Bucket> bucketsInS3 = getBucketListFromS3();
        Map<String, BucketMetadata> bucketsMetadata = accessManagementAPIService.getAllBucketsMetaDataByUserName( userName ).stream()
                .collect( Collectors.toMap( BucketMetadata::getBucketName, Function.identity() ) );
        if (!bucketsInS3.isEmpty() && !bucketsMetadata.isEmpty()) {
            bucketsInS3.forEach( bucket -> bucket.setAccess( bucketsMetadata.get( bucket.getName() ) ) );
        }
//        System.out.println("bucketsInS3----->"+bucketsInS3 );
//        System.out.println("bucketsMetadata----->"+bucketsMetadata );
//        System.out.println( "Buckets with Access ------>" + bucketsInS3 );
        return bucketsInS3;
    }


    public String getFilesAndFoldersByUserNameAndBucketName(String userName, String bucketName) {
        log.info( "Inside getFilesAndFoldersByUserAndBucket" );
        return s3ReadService.listObjectsWithMetadata( userName, bucketName );
    }

    private FileToBeCached getCachedObject(String objectName, String bucketName) {
        return cacheManager.getCachedObject( bucketName + "/" + objectName );
    }

    private BASE64DecodedMultipartFile convertCachedFileToBASE64DecodedMultipartFile(FileToBeCached cachedObject) {
        byte[] cachedFileToBeRetrievedInByteArrayFormat = Base64.getDecoder().decode( cachedObject.getFileContentInBase64().getBytes( StandardCharsets.UTF_8 ) );
        return new BASE64DecodedMultipartFile( cachedFileToBeRetrievedInByteArrayFormat );
    }

    private BASE64DecodedMultipartFile convertRawS3ObjectIntoBase64(S3Object s3Object) throws IOException {
        byte[] downloadedObjectInByteArrayFormat = ByteStreams.toByteArray( s3Object.getObjectContent() );
        return new BASE64DecodedMultipartFile( downloadedObjectInByteArrayFormat );
    }

    public S3DownloadedObject downloadFile(String objectName, String bucketName) {
        log.info( "Inside downloadFile" );

        try {
            FileToBeCached cachedObject = getCachedObject( objectName, bucketName );
            if (null != cachedObject) {
                return new S3DownloadedObject( objectName, bucketName, convertCachedFileToBASE64DecodedMultipartFile( cachedObject ).getResource() );
            } else {
                log.info( "Requested Resource Doesn't Exist in Cache" );
                if (!lockServerAPIService.getLockStatusForGivenObject( objectName )) {
                    BASE64DecodedMultipartFile downloadedObjectInMultipartFile = convertRawS3ObjectIntoBase64( Objects.requireNonNull( s3ReadService.getObject( objectName, bucketName ) ) );
                    S3DownloadedObject s3DownloadedObject = new S3DownloadedObject( objectName, bucketName, downloadedObjectInMultipartFile.getResource() );
                    CacheInsertionThread cacheInsertionThread = new CacheInsertionThread( cacheManager, new DownloadedCacheObject( objectName, bucketName, Objects.requireNonNull( downloadedObjectInMultipartFile ) ) );
                    cacheInsertionThread.thread.start();

                    return s3DownloadedObject;
                }
            }
        } catch (Exception e) {
            log.error( "Exception while downloading the file" + e.getMessage() + e.getCause() );
        }
        return null;
    }


    private DownloadedObject downloadFileInBase64(S3DownloadObject s3DownloadObject) {
        try {
            FileToBeCached cachedObject = getCachedObject( s3DownloadObject.getObjectName(), s3DownloadObject.getBucketName() );
            if (null != cachedObject) {
                return new DownloadedObject( s3DownloadObject.getFileName(), s3DownloadObject.getObjectName(), s3DownloadObject.getBucketName(), cachedObject.getFileContentInBase64() );
            } else {
                S3Object s3Object = (Objects.requireNonNull( s3ReadService.getObject( s3DownloadObject.getObjectName(), s3DownloadObject.getBucketName() ) ));
                byte[] contentBytes = s3Object.getObjectContent().readAllBytes();
                s3Object.getObjectContent().close();
                CacheInsertionThread cacheInsertionThread = new CacheInsertionThread( cacheManager, new DownloadedCacheObject( s3DownloadObject.getObjectName(), s3DownloadObject.getBucketName(), new BASE64DecodedMultipartFile( contentBytes ) ) );
                cacheInsertionThread.thread.start();
                return new DownloadedObject( s3DownloadObject.getFileName(), s3DownloadObject.getObjectName(), s3DownloadObject.getBucketName(), Base64.getEncoder().encodeToString( contentBytes ) );
            }
        } catch (Exception e) {
            log.error( "Exception while downloading the file in base64" + e.getMessage() + e.getCause() );
        }
        return null;
    }


    public List<DownloadedObject> downloadFolder(DownloadFolderRequest objectsToBeDownloaded) {
        log.info( "Inside downloadFolder" );
        try {
            if (!lockServerAPIService.getLockStatusForGivenObjects( objectsToBeDownloaded.getObjectsToBeDownloaded().get( 0 ).getObjectName() )) {
                return objectsToBeDownloaded.getObjectsToBeDownloaded().stream()
//                        .filter( s3DownloadObject -> !s3DownloadObject.getObjectName().endsWith( "/" ) ) not needed
                        .map( s3DownloadObject -> Objects.requireNonNull( downloadFileInBase64( s3DownloadObject ) ) )
                        .collect( Collectors.toList() );
            }
        } catch (Exception e) {

        }
        return null;
    }


    @KafkaListener(groupId = "readConsumer", topics = "read")
    public void consume(String bucketName, ConsumerRecord record) {


        if (record.key() == ("add")) {
            log.info( "Consumed Cache add Event" );
            Bucket bucketToBeAddedInCache = new Bucket( bucketName );
            if (!bucketList.isEmpty()) {
                bucketList.add( bucketToBeAddedInCache );
                log.info( "Bucket has been added in the cache" );
            }

        }
        if (record.key() == ("delete")) {
            log.info( "Consumed Cache delete Event" );
            if (!bucketList.isEmpty()) {
                bucketList.removeIf( bucket -> bucket.getName().equals( bucketName ) );
                log.info( "Bucket has been deleted from the cache" );
            }

        }
    }
}
