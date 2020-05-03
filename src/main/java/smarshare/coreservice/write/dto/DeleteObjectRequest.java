package smarshare.coreservice.write.dto;


import lombok.Data;

public @Data
class DeleteObjectRequest {

    private String objectName;
    private String bucketName;
    private int ownerId;
}
