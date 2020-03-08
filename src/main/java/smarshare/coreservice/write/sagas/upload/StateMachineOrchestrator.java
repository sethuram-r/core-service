package smarshare.coreservice.write.sagas.upload;


import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.sagas.constants.UploadStateNames;
import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;

import java.util.List;
import java.util.UUID;

public class StateMachineOrchestrator {

    private UploadStates uploadStates;
    private SagaEventWrapper orchestratorObject;


    public StateMachineOrchestrator(List<UploadObject> input) {
        this.orchestratorObject = wrapInputIntoOrchestratorObject( input );
        uploadStates = new UploadStates();
    }

    private SagaEventWrapper wrapInputIntoOrchestratorObject(List<UploadObject> filesToUpload) {
        return new SagaEventWrapper( filesToUpload, UUID.randomUUID().toString() );
    }


    public SagaEventWrapper orchestrate() {

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
