package smarshare.coreservice.read.model.filestructure;

import smarshare.coreservice.read.dto.AccessingUserInfoForApi;

import java.io.Serializable;

public class AccessInfo  implements Serializable {

    private Boolean read;
    private Boolean write;
    private Boolean delete;

    public AccessInfo(Boolean read, Boolean write, Boolean delete) {
        this.read = read;
        this.write = write;
        this.delete = delete;
    }

    public AccessInfo(AccessingUserInfoForApi accessingUserInfo) {
        this.read = accessingUserInfo.getRead();
        this.write = accessingUserInfo.getWrite();
        this.delete = accessingUserInfo.getDelete();
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
