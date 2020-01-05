package smarshare.coreservice.cache.model;

public class FileToBeCached {

    private String fileName;
    private String fileContentInBase64;

    public FileToBeCached(String fileName, String fileContentInBase64) {
        this.fileName = fileName;
        this.fileContentInBase64 = fileContentInBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileContentInBase64() {
        return fileContentInBase64;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileContentInBase64(String fileContentInBase64) {
        this.fileContentInBase64 = fileContentInBase64;
    }

    @Override
    public String toString() {
        return "FileToBeCached{" +
                "fileName='" + fileName + '\'' +
                ", fileContentInBase64='" + fileContentInBase64 + '\'' +
                '}';
    }
}
