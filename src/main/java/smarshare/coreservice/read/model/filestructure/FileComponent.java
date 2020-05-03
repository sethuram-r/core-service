package smarshare.coreservice.read.model.filestructure;

import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
public @Data
class FileComponent extends BucketComponent {

    public FileComponent(String name, AccessInfo accessInfo, String owner, int ownerId, String completeName, String lastModified) {
        this.name = name;
        this.accessInfo = accessInfo;
        this.owner = owner;
        this.ownerId = ownerId;
        this.completeName = completeName;
        this.lastModified = lastModified;
    }

}
