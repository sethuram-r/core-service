package smarshare.coreservice.read.model.filestructure;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

public class BASE64DecodedMultipartFile implements MultipartFile {
    private final byte[] fileContent;

    public BASE64DecodedMultipartFile(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    @Override
    public String getName() {
        // TODO - implementation depends on your requirements
        return null;
    }

    @Override
    public String getOriginalFilename() {
        // TODO - implementation depends on your requirements
        return null;
    }

    @Override
    public String getContentType() {
        // TODO - implementation depends on your requirements
        return null;
    }

    @Override
    public boolean isEmpty() {
        return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream( fileContent );
    }

    @Override
    public Resource getResource() {
        return new ByteArrayResource( this.fileContent );
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {

    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        new FileOutputStream( dest ).write( fileContent );
    }
}