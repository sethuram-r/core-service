package smarshare.coreservice.write.model.lock;


public class File {

    private String fileName;
    private Boolean lockStatus;

    public File(String fileName, Boolean lockStatus) {
        this.fileName = fileName;
        this.lockStatus = lockStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public Boolean getLockStatus() {
        return lockStatus;
    }

}
