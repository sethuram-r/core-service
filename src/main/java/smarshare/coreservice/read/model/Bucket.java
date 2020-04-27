package smarshare.coreservice.read.model;


import lombok.Data;
import smarshare.coreservice.read.dto.BucketAccess;
import smarshare.coreservice.read.dto.BucketMetadata;

public @Data
class Bucket {

    private String name;
    private BucketAccess access = new BucketAccess();

    public Bucket(String name) {
        this.name = name;
    }

    public Bucket setAccess(BucketMetadata bucketMetadata) {
        this.access.setRead( bucketMetadata.getRead() );
        this.access.setWrite( bucketMetadata.getWrite() );
        return this;
    }
}
