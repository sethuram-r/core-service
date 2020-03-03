package smarshare.coreservice.read.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public @Data
class S3DownloadObject {

    private String fileName;
    private String objectName; // complete name with path and file name;
    private String bucketName;


}
