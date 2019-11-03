package smarshare.coreservice.read.model;


import smarshare.coreservice.read.dto.BucketMetadata;

public class Bucket {

    private String name;
    private BucketMetadata bucketMetadata;

    public BucketMetadata getBucketMetadata() {
        return bucketMetadata;
    }

    public void setBucketMetadata(BucketMetadata bucketMetadata) {
        this.bucketMetadata = bucketMetadata;
    }

    public Bucket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "name='" + name + '\'' +
                '}';
    }
}
