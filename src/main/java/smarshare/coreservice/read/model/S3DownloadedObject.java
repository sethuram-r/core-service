package smarshare.coreservice.read.model;

import org.springframework.core.io.Resource;

public class S3DownloadedObject {

    S3DownloadObject s3DownloadObject;
    Resource downloadedObjectResource;

    public S3DownloadedObject(S3DownloadObject s3DownloadObject, Resource downloadedObjectResource) {
        this.s3DownloadObject = s3DownloadObject;
        this.downloadedObjectResource = downloadedObjectResource;
    }

    public S3DownloadObject getS3DownloadObject() {
        return s3DownloadObject;
    }

    public Resource getDownloadedObjectResource() {
        return downloadedObjectResource;
    }
}
