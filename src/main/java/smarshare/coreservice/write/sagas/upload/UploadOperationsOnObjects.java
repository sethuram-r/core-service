package smarshare.coreservice.write.sagas.upload;

import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;

@FunctionalInterface
public interface UploadOperationsOnObjects {
    Boolean apply(SagaEventWrapper taskInput);
}
