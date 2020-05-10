//package smarshare.coreservice.write.helper;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Scope;
//import smarshare.coreservice.write.model.UploadObject;
//import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;
//import smarshare.coreservice.write.sagas.upload.StateMachineOrchestrator;
//
//import java.util.List;
//
//@Slf4j
//@Scope("prototype")
//public class SagaOrchestratorThread implements Runnable {
//
//    public Thread thread;
//    StateMachineOrchestrator stateMachineOrchestrator;
//
//
//    public SagaOrchestratorThread(List<UploadObject> uploadObjects) {
//        thread = new Thread( this, "Saga Upload Orchestrator Thread" );
//        this.stateMachineOrchestrator = new StateMachineOrchestrator(  );
//
//    }
//
//    @Override
//    public void run() {
//        log.info( "Saga Upload Orchestrator starts....." );
//        SagaEventWrapper orchestrate = stateMachineOrchestrator.orchestrate();
//        log.info( "Saga Upload Orchestrator ends...." + orchestrate.getRecentSuccessfulState() );
//    }
//}
