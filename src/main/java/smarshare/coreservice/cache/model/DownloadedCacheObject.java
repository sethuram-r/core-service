package smarshare.coreservice.cache.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import smarshare.coreservice.read.model.filestructure.BASE64DecodedMultipartFile;

@AllArgsConstructor
public @Data
class DownloadedCacheObject {
    private String objectName;
    private String bucketName;
    private BASE64DecodedMultipartFile fileContentInBase64;

}
