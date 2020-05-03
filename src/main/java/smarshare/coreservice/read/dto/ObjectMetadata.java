package smarshare.coreservice.read.dto;


import lombok.Data;

public @Data
class ObjectMetadata {

    private String ownerName;
    private int ownerId;
    private AccessingUserInfoForApi accessingUserInfo;

}
