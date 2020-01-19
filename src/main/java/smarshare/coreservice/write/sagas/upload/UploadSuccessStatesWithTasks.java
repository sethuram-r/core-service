package smarshare.coreservice.write.sagas.upload;

import com.amazonaws.services.s3.transfer.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.sagas.State;
import smarshare.coreservice.write.service.WriteService;

import java.util.List;

@Slf4j
public enum UploadSuccessStatesWithTasks implements State {

    UPLOAD_REQUEST_ACCEPTED {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                this.state = LOCK_EVENT_SENT_TO_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }
    },
    LOCK_EVENT_SENT_TO_KAFKA() {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                Boolean isProducerRecordMetadataEmpty = State.uploadFunction( successStateOperations::lockEventToKafka, taskInput );
                this.state = isProducerRecordMetadataEmpty ? UploadFailureStatesWithTasks.UPLOAD_REQUEST_REJECTED : LOCK_EVENT_RESULT_FROM_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }
    },
    LOCK_EVENT_RESULT_FROM_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            // have to implement producer in lock service and consumer here
            try {
                List<FileToUpload> consumedRecord = State.uploadFunction( successStateOperations::getConsumedLockResultRecord, taskInput );
                this.state = consumedRecord.isEmpty() ? UploadFailureStatesWithTasks.UPLOAD_REQUEST_REJECTED : S3_UPLOAD_AND_CACHE_TASK;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }
    },
    S3_UPLOAD_AND_CACHE_TASK {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                List<Transfer.TransferState> uploadedResult = State.uploadFunction( writeService::uploadObjectToS3, taskInput );
                boolean areUploadedResultsInCompleteState = uploadedResult.stream().allMatch( transferState -> transferState.equals( Transfer.TransferState.Completed ) );
                this.state = areUploadedResultsInCompleteState ? ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_TO_KAFKA : UploadFailureStatesWithTasks.UNLOCK_EVENT_TO_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }
    },
    ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_TO_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                Boolean isProducerRecordMetadataEmpty = State.uploadFunction( successStateOperations::accessManagementServiceCreateEntryEventToKafka, taskInput );
                // have to change failure state
                this.state = isProducerRecordMetadataEmpty ? UploadFailureStatesWithTasks.S3_AND_CACHE_DELETE_TASK : ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_RESULT_FROM_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }
    },
    ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_RESULT_FROM_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                List<FileToUpload> consumedRecord = State.uploadFunction( successStateOperations::getConsumedAccessRecord, taskInput );
                this.state = consumedRecord.isEmpty() ? UploadFailureStatesWithTasks.S3_AND_CACHE_DELETE_TASK : COMPLETED;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();

        }
    },
    COMPLETED {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> filesToUpload) {
            return "Completed";
        }
    };


    State state;

    @Autowired
    WriteService writeService;
    @Autowired
    SuccessStateOperations successStateOperations;


}
