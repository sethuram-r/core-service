package smarshare.coreservice.read.model;

import java.util.List;

public class Bucket {

    private String name;
    private List<String> bucketObjects;

    public Bucket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getBucketObjects() {
        return bucketObjects;
    }

    public void setBucketObjects(List<String> bucketObjects) {
        this.bucketObjects = bucketObjects;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "name='" + name + '\'' +
                ", bucketObjects=" + bucketObjects +
                '}';
    }
}
