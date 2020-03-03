package smarshare.coreservice.read.dto;

import lombok.Data;

import java.util.List;

public @Data
class DownloadFolderRequest {

    private List<S3DownloadObject> objectsToBeDownloaded;
}
