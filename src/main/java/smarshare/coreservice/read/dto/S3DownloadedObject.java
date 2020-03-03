package smarshare.coreservice.read.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@AllArgsConstructor
public @Data
class S3DownloadedObject {

    private String objectName;
    private String bucketName;
    private Resource downloadedObjectResource;

}
