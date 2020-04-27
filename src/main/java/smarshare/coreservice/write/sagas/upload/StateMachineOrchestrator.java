package smarshare.coreservice.write.sagas.upload;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.sagas.constants.UploadStateNames;
import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class StateMachineOrchestrator {


    private UploadStates uploadStates;
    private SagaEventWrapper orchestratorObject;


    @Autowired
    public StateMachineOrchestrator(UploadStates uploadStates) {
        this.uploadStates = uploadStates;
        this.uploadStates.initializeStates();
    }

    private SagaEventWrapper wrapInputIntoOrchestratorObject(List<UploadObject> filesToUpload) {
        return new SagaEventWrapper( filesToUpload, UUID.randomUUID().toString() );
    }


    public SagaEventWrapper orchestrate(List<UploadObject> input) {
        log.info( " ***********   Orchestration Begins  *********" );
        this.orchestratorObject = wrapInputIntoOrchestratorObject( input );
        StateTemplate currentStateBeingExecuted = uploadStates.getInitialStateForUploadOrchestrator();

        while (!(orchestratorObject.getRecentSuccessfulState().equals( UploadStateNames.UPLOAD_REQUEST_REJECTED.valueOf() )
                || orchestratorObject.getRecentSuccessfulState().equals( UploadStateNames.COMPLETED.valueOf() )
                || orchestratorObject.getRecentSuccessfulState().equals( UploadStateNames.ERROR.valueOf() ))) {
            this.orchestratorObject = currentStateBeingExecuted.handleTask( this.orchestratorObject );
            if (!currentStateBeingExecuted.isTerminalState())
                currentStateBeingExecuted = uploadStates.getUploadStateByName( currentStateBeingExecuted.getNextState() );
        }

        return this.orchestratorObject;
    }


}
