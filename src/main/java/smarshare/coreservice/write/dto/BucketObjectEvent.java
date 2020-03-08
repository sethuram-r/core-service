package smarshare.coreservice.write.dto;


import lombok.Data;

public @Data
class BucketObjectEvent {

    private String bucketName;
    private String objectName;
    private String ownerName;
    private String userName;
    private String status = "In process";

}
