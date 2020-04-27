package smarshare.coreservice.read.dto;


import lombok.Data;


public @Data
class BucketMetadata {


    private String bucketName;
    private Boolean read;
    private Boolean write;


}
