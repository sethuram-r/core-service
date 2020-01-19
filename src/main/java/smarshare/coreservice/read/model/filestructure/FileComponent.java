package smarshare.coreservice.read.model.filestructure;

public class FileComponent extends BucketComponent {

    public FileComponent(String name, AccessInfo accessInfo, String owner, String completeName, String lastModified) {
        this.name = name;
        this.accessInfo = accessInfo;
        this.owner = owner;
        this.completeName = completeName;
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "FileComponent{" +
                "name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", accessInfo=" + accessInfo +
                '}';
    }
}
