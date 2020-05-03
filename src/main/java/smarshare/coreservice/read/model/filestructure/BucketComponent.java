package smarshare.coreservice.read.model.filestructure;


public abstract class BucketComponent {


    String name;
    String owner;
    int ownerId;
    AccessInfo accessInfo;
    String completeName;
    String lastModified;

    public String getName() {
        return name;
    }

    public AccessInfo getAccessInfo() {
        return accessInfo;
    }

    public String getOwner() {
        return owner;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getCompleteName() {
        return completeName;
    }

    public String getLastModified() {
        return lastModified;
    }
}
