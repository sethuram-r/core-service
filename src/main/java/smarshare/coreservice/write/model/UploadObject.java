package smarshare.coreservice.write.model;

import lombok.Data;
import lombok.ToString;

@ToString
public @Data
class UploadObject {

    String objectName;
    String content;
    String owner;
    int ownerId;
    String bucketName;
    AccessInfo defaultAccessInfo = new AccessInfo( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE );
}
