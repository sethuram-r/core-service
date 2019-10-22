package smarshare.coreservice.write.model;


public class AccessInfo {

    private Boolean read;
    private Boolean write;
    private Boolean delete;

    public AccessInfo(Boolean read, Boolean write, Boolean delete) {
        this.read = read;
        this.write = write;
        this.delete = delete;
    }

    public Boolean getRead() {
        return read;
    }

    public Boolean getWrite() {
        return write;
    }

    public Boolean getDelete() {
        return delete;
    }

    @Override
    public String toString() {
        return "AccessInfo{" +
                "read=" + read +
                ", write=" + write +
                ", delete=" + delete +
                '}';
    }
}
