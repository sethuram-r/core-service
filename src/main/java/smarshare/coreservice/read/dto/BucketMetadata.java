package smarshare.coreservice.read.dto;

public class BucketMetadata {

    private String userName;
    private Boolean read;
    private Boolean write;

    public String getUserName() {
        return userName;
    }

    public Boolean getRead() {
        return read;
    }

    public Boolean getWrite() {
        return write;
    }

    @Override
    public String toString() {
        return "BucketMetadata{" +
                "userName='" + userName + '\'' +
                ", read=" + read +
                ", write=" + write +
                '}';
    }
}
