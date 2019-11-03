package smarshare.coreservice.read.dto;

public class AccessingUserInfoForApi {

    private String userName;
    private Boolean read;
    private Boolean write;
    private Boolean delete;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getWrite() {
        return write;
    }

    public void setWrite(Boolean write) {
        this.write = write;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return "AccessingUsersInfoForApi{" +
                "userName='" + userName + '\'' +
                ", read=" + read +
                ", write=" + write +
                ", delete=" + delete +
                '}';
    }
}
