package smarshare.coreservice.write.model;

import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
public class FilesToUpload implements Serializable {

    List<FileToUpload> filesToUpload;

    public List<FileToUpload> getFilesToUpload() {
        return filesToUpload;
    }
}
