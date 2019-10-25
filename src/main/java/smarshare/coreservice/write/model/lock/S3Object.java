package smarshare.coreservice.write.model.lock;


public class S3Object {

    private String objectName;
    private Boolean lockStatus;

    public S3Object(String objectName, Boolean lockStatus) {
        this.objectName = objectName;
        this.lockStatus = lockStatus;
    }

    public String getObjectName() {
        return objectName;
    }

    public Boolean getLockStatus() {
        return lockStatus;
    }


}
