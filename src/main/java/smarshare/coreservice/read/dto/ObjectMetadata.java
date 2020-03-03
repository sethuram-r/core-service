package smarshare.coreservice.read.dto;


import lombok.Data;

public @Data
class ObjectMetadata {

    private String ownerName;
    private AccessingUserInfoForApi accessingUserInfo;

}
