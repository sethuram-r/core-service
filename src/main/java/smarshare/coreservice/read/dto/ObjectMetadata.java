package smarshare.coreservice.read.dto;


public class ObjectMetadata {

    private String ownerName;
    private AccessingUserInfoForApi accessingUserInfo;

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public AccessingUserInfoForApi getAccessingUserInfo() {
        return accessingUserInfo;
    }

    public void setAccessingUserInfo(AccessingUserInfoForApi accessingUserInfo) {
        this.accessingUserInfo = accessingUserInfo;
    }

    @Override
    public String toString() {
        return "ObjectMetadata{" +
                "ownerName='" + ownerName + '\'' +
                ", accessingUserInfo=" + accessingUserInfo +
                '}';
    }
}
