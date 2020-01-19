package smarshare.coreservice.write.sagas.upload;

import smarshare.coreservice.write.model.FileToUpload;

import java.util.List;

@FunctionalInterface
public interface UploadOperationsOnObjects {
    Object apply(List<FileToUpload> filesToUpload);
}
