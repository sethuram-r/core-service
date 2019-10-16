package smarshare.coreservice.read.service;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.model.Bucket;

import java.util.ArrayList;
import java.util.List;

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

    public List<Bucket> getFilesAndFoldersByUserAndBucket(String userName, String bucketName){
        log.info( "Inside getFilesAndFoldersByUserAndBucket" );
        s3Service.listObjects(userName, bucketName );
        return null;
    }

}
