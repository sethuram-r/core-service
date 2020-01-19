package smarshare.coreservice.write.sagas;


import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.sagas.upload.UploadOperationsOnObjects;

import java.util.List;

public interface State {
    static <U> U uploadFunction(UploadOperationsOnObjects uploadOperationsOnObjects, List<FileToUpload> filesToUpload) {
        return (U) uploadOperationsOnObjects.apply( filesToUpload );
    }

    State nextState();

    String taskToBeDoneInThisState(List<FileToUpload> filesToUpload);


}
