package smarshare.coreservice.read.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import smarshare.coreservice.read.model.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smarshare.coreservice.read.service.helper.BucketObjectsHelper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class S3Service {

    private AmazonS3 amazonS3Client;
    private BucketObjectsHelper bucketObjectsHelper;

    @Autowired
    S3Service(AmazonS3 amazonS3Client , BucketObjectsHelper bucketObjectsHelper){
        this.amazonS3Client = amazonS3Client;
        this.bucketObjectsHelper = bucketObjectsHelper;
    }

    public List<Bucket> listBuckets(){
        log.info( "Inside listBuckets" );
        List<Bucket> bucketList  = new ArrayList<>();
        List<com.amazonaws.services.s3.model.Bucket> rawBucketListFromS3 = amazonS3Client.listBuckets();
        if(!rawBucketListFromS3.isEmpty()){
            rawBucketListFromS3.forEach( bucket -> {
                bucketList.add( new Bucket(bucket.getName()  ) );
            } );
        }
        return bucketList;
    }


    private List<String> getObjectKeys(String userName,String bucketName){
        List<String> objectKeysObtainedFromS3 = new ArrayList<>(  );
        List<S3ObjectSummary>  objectsInRawFormat = amazonS3Client.listObjectsV2(bucketName).getObjectSummaries();
        if(!objectsInRawFormat.isEmpty()){
            objectsInRawFormat.forEach( eachObject -> {
                objectKeysObtainedFromS3.add( eachObject.getKey() );
            } );
        }
        System.out.println(objectKeysObtainedFromS3);
        bucketObjectsHelper.convertKeysInFileStructureFormat( objectKeysObtainedFromS3, userName, bucketName);
        return null;
    }

    public void listObjects(String userName,String bucketName){
        log.info( "Inside listObjects" );
        getObjectKeys(userName, bucketName );


    }
}
