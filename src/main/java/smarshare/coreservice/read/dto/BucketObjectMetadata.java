package smarshare.coreservice.read.dto;

import lombok.Data;

public @Data
class BucketObjectMetadata {

    private String objectName;
    private ObjectMetadata objectMetadata;

}
