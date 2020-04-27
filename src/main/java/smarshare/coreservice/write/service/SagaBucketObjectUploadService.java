package smarshare.coreservice.write.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.sagas.constants.UploadStateNames;
import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;
import smarshare.coreservice.write.sagas.upload.StateMachineOrchestrator;

import java.util.List;

@Slf4j
@Service
public class SagaBucketObjectUploadService {

    private StateMachineOrchestrator stateMachineOrchestrator;

    @Autowired
    public SagaBucketObjectUploadService(StateMachineOrchestrator stateMachineOrchestrator) {
        this.stateMachineOrchestrator = stateMachineOrchestrator;
    }

    public ResponseEntity<Boolean> UploadObjectThroughSaga(List<UploadObject> filesToUpload) {
        log.info( "inside UploadObjectThroughSaga " );
        try {
            SagaEventWrapper orchestrate = this.stateMachineOrchestrator.orchestrate( filesToUpload );
            log.debug( "Recent Successfully completed State :" + orchestrate.getRecentSuccessfulState() );
            if (orchestrate.getRecentSuccessfulState().equals( UploadStateNames.COMPLETED.valueOf() )) {
                return ResponseEntity.ok( true );
            } else {
                new ResponseEntity<>( false, HttpStatus.EXPECTATION_FAILED );
            }

        } catch (Exception e) {
            log.error( "Exception while starting Saga Upload Orchestrator Thread " + e );

        }
        return new ResponseEntity<>( false, HttpStatus.BAD_REQUEST );
    }
}
