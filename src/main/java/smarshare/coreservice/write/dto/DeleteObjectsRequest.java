package smarshare.coreservice.write.dto;

import lombok.Data;

import java.util.List;

public @Data
class DeleteObjectsRequest {

    private List<DeleteObjectRequest> folderObjects;
    private String bucketName;
}
