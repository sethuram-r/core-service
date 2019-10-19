package smarshare.coreservice.read.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.model.Bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReadService {

    private S3Service s3Service;
    private List<Bucket> bucketList = null;

    @Autowired
    ReadService(S3Service s3Service){
        this.s3Service = s3Service;
    }

    public List<Bucket> getBucketListFromS3() {
        log.info( "Inside getBucketListFromS3" );
        if (bucketList == null){
            bucketList = s3Service.listBuckets();
        }
        return bucketList;
    }

    public List<Bucket> getFilesAndFoldersListByUserAndBucket(String userName, String bucketName) {
        log.info( "Inside getFilesAndFoldersByUserAndBucket" );
        s3Service.listObjects(userName, bucketName );
        return null;
    }

    public Map<String, Resource> downloadFile(String objectName, String fileName, String bucketName) {
        log.info( "Inside downloadFile" );
        /* have to implement cache logic */
        return s3Service.getObject( objectName, fileName, bucketName );
    }

    public List<Map<String, Resource>> downloadFolder(Map<String, Map<String, String>> fileNameWrapper) {
        log.info( "Inside downloadFolder" );
        List<Map<String, Resource>> downloadedFiles = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> eachFile : fileNameWrapper.entrySet()) {
            eachFile.getValue().forEach( (objectName, bucketName) -> {
                downloadedFiles.add( s3Service.getObject( objectName, eachFile.getKey(), bucketName ) );
            } );
        }
        return (downloadedFiles);


    }
}
