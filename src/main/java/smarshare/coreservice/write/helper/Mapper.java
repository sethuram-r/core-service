package smarshare.coreservice.write.helper;

import smarshare.coreservice.write.dto.BucketObjectForEvent;
import smarshare.coreservice.write.model.FileToUpload;

public class Mapper {

    public static BucketObjectForEvent mappingUploadObjectToBucketObjectEvent(FileToUpload fileToUpload) {
        BucketObjectForEvent bucketObjectForEvent = new BucketObjectForEvent();
        bucketObjectForEvent.setBucketName( fileToUpload.getBucketName() );
        bucketObjectForEvent.setObjectName( fileToUpload.getUploadedFileName() );
        bucketObjectForEvent.setOwnerName( fileToUpload.getOwnerOfTheFile() );
        bucketObjectForEvent.setUserName( fileToUpload.getOwnerOfTheFile() );
        return bucketObjectForEvent;
    }
}
