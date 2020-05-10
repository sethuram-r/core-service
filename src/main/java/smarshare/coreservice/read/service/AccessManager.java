package smarshare.coreservice.read.service;

public interface AccessManager {
    String getFilesAndFoldersByUserIdAndBucketName(int userId, String bucketName);
}
