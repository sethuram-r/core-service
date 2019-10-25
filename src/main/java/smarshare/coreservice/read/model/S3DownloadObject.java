package smarshare.coreservice.read.model;

public class S3DownloadObject {

    private String fileName;
    private String objectName; // complete name with path and file name;
    private String bucketName;

    public S3DownloadObject(String fileName, String objectName, String bucketName) {
        this.fileName = fileName;
        this.objectName = objectName;
        this.bucketName = bucketName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Override
    public String toString() {
        return "S3UploadObject{" +
                "fileName='" + fileName + '\'' +
                ", objectName='" + objectName + '\'' +
                ", bucketName='" + bucketName + '\'' +
                '}';
    }
}
