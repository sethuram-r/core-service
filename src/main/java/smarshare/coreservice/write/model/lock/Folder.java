package smarshare.coreservice.write.model.lock;


import java.util.List;

public class Folder {


    private String folderName;
    private List<File> files;
    private Boolean lockStatus;


    public Folder(String folderName, List<File> fileLocks, Boolean lockStatus) {
        this.folderName = folderName;
        this.files = fileLocks;
        this.lockStatus = lockStatus;

    }

    public List<File> getFiles() {
        return files;
    }

    public String getFolderName() {
        return folderName;
    }

    public Boolean getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(Boolean lockStatus) {
        this.lockStatus = lockStatus;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "folderName='" + folderName + '\'' +
                ", files=" + files +
                ", lockStatus=" + lockStatus +
                '}';
    }
}
