package smarshare.coreservice.write.model;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class FileToUpload {

    String uploadedFileName;
    String uploadedFileContent;
    String ownerOfTheFile;
    String selectedFolderWhereFolderHasToBeUploaded;
    String bucketName;
    AccessInfo defaultAccessInfo = new AccessInfo( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE );

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public String getUploadedFileContent() {
        return uploadedFileContent;
    }

    public FileToUpload setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
        return this;
    }

    public FileToUpload setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public AccessInfo getDefaultAccessInfo() {
        return defaultAccessInfo;
    }

    public String getOwnerOfTheFile() {
        return ownerOfTheFile;
    }

    public String getSelectedFolderWhereFolderHasToBeUploaded() {
        return selectedFolderWhereFolderHasToBeUploaded;
    }

    public String getBucketName() {
        return bucketName;
    }

    public AccessInfo getAccessInfo() {
        return defaultAccessInfo;
    }

    @Override
    public String toString() {
        return "FileToUpload{" +
                "uploadedFileName='" + uploadedFileName + '\'' +
                ", uploadedFileContent='" + uploadedFileContent + '\'' +
                ", ownerOfTheFile='" + ownerOfTheFile + '\'' +
                ", selectedFolderWhereFolderHasToBeUploaded='" + selectedFolderWhereFolderHasToBeUploaded + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", defaultAccessInfo=" + defaultAccessInfo +
                '}';
    }
}
