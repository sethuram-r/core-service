package smarshare.coreservice.write.model;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class Folder {

    // Have to include access info

    private String name;
    private List<MultipartFile> files;

    Folder(String name, List<MultipartFile> files) {
        this.name = name;
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", files=" + files +
                '}';
    }
}
