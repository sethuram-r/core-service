package smarshare.coreservice.write.helper;

import smarshare.coreservice.write.dto.BucketObjectEvent;
import smarshare.coreservice.write.model.UploadObject;

public class Mapper {

    public static BucketObjectEvent mappingUploadObjectToBucketObjectEvent(UploadObject uploadObject) {
        BucketObjectEvent bucketObjectEvent = new BucketObjectEvent();
        bucketObjectEvent.setBucketName( uploadObject.getBucketName() );
        bucketObjectEvent.setObjectName( uploadObject.getObjectName() );
        bucketObjectEvent.setOwnerName( uploadObject.getOwner() );
        bucketObjectEvent.setUserName( uploadObject.getOwner() );
        return bucketObjectEvent;
    }
}
