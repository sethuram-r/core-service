package smarshare.coreservice.read.model.filestructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import smarshare.coreservice.read.dto.AccessingUserInfoForApi;

import java.io.Serializable;

@AllArgsConstructor
public @Data
class AccessInfo implements Serializable {

    private Boolean read;
    private Boolean write;
    private Boolean delete;

    public AccessInfo(AccessingUserInfoForApi accessingUserInfo) {
        this.read = accessingUserInfo.getRead();
        this.write = accessingUserInfo.getWrite();
        this.delete = accessingUserInfo.getDelete();
    }
}
