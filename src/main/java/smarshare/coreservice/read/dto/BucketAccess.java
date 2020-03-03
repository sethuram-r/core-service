package smarshare.coreservice.read.dto;

import lombok.Data;

public @Data
class BucketAccess {
    private Boolean read;
    private Boolean write;

}
