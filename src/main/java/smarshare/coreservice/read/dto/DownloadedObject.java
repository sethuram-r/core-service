package smarshare.coreservice.read.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data
class DownloadedObject extends S3DownloadObject {
    private String downloadedObjectInBase64;

    public DownloadedObject(String fileName, String objectName, String bucketName, String downloadedObjectInBase64) {
        super( fileName, objectName, bucketName );
        this.downloadedObjectInBase64 = downloadedObjectInBase64;
    }
}
