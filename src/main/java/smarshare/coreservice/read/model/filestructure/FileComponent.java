package smarshare.coreservice.read.model.filestructure;

import lombok.Data;


public @Data
class FileComponent extends BucketComponent {

    public FileComponent(String name, AccessInfo accessInfo, String owner, String completeName, String lastModified) {
        this.name = name;
        this.accessInfo = accessInfo;
        this.owner = owner;
        this.completeName = completeName;
        this.lastModified = lastModified;
    }

}
