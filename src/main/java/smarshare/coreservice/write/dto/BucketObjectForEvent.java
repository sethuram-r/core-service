package smarshare.coreservice.write.dto;

public class BucketObjectForEvent {

    private String bucketName;
    private String objectName;
    private String ownerName;
    private String userName;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getUserName() {
        return ownerName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "BucketObjectFromApi{" +
                "bucketName='" + bucketName + '\'' +
                ", objectName='" + objectName + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", userName='" + ownerName + '\'' +
                '}';
    }
}
