package smarshare.coreservice.write.sagas.upload;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.write.model.FileToUpload;
import smarshare.coreservice.write.model.Status;
import smarshare.coreservice.write.sagas.State;
import smarshare.coreservice.write.service.WriteService;

import java.util.List;

@Slf4j
public enum UploadFailureStatesWithTasks implements State {

    UPLOAD_REQUEST_REJECTED {
        @Override
        public State nextState() {
            return this;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            return "Rejected";
        }

    },
    UNLOCK_EVENT_TO_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> filesToUpload) {
            try {
                Boolean isProducerRecordMetadataEmpty = State.uploadFunction( failureStateOperations::unLockEventToKafka, filesToUpload );
                this.state = isProducerRecordMetadataEmpty ? ERROR : UNLOCK_EVENT_RESULT_FROM_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
                this.state = ERROR;
            }
            return this.name();
        }
    },
    UNLOCK_EVENT_RESULT_FROM_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            // have to implement producer in lock service
            try {
                List<FileToUpload> consumedRecord = State.uploadFunction( failureStateOperations::getConsumedUnLockResultRecord, taskInput );
                this.state = consumedRecord.isEmpty() ? ERROR : UPLOAD_REQUEST_REJECTED;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
                this.state = ERROR;
            }
            return this.name();
        }
    },
    S3_AND_CACHE_DELETE_TASK {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                List<Status> deletionResult = State.uploadFunction( writeService::deleteObjectsForSagaFromS3, taskInput );
                boolean areDeletionResultsInSuccessState = deletionResult.stream().allMatch( status -> status.getMessage().equals( "Success" ) );
                this.state = areDeletionResultsInSuccessState ? ERROR : UploadFailureStatesWithTasks.UNLOCK_EVENT_TO_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }

    },
    ACCESS_MANAGEMENT_ENTRY_DELETE_EVENT_TO_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                Boolean isProducerRecordMetadataEmpty = State.uploadFunction( failureStateOperations::accessManagementServiceDeleteEntryEventToKafka, taskInput );
                this.state = isProducerRecordMetadataEmpty ? ERROR : ACCESS_MANAGEMENT_ENTRY_DELETE_EVENT_RESULT_FROM_KAFKA;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();
        }

    },
    ACCESS_MANAGEMENT_ENTRY_DELETE_EVENT_RESULT_FROM_KAFKA {
        @Override
        public State nextState() {
            return this.state;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> taskInput) {
            try {
                List<FileToUpload> consumedRecord = State.uploadFunction( failureStateOperations::getConsumedAccessRecord, taskInput );
                this.state = consumedRecord.isEmpty() ? UploadFailureStatesWithTasks.ERROR : S3_AND_CACHE_DELETE_TASK;
            } catch (Exception e) {
                log.error( "Exception in " + this.state.toString() + " " + e.getMessage() );
            }
            return this.name();

        }
    },
    ERROR {
        @Override
        public State nextState() {
            return null;
        }

        @Override
        public String taskToBeDoneInThisState(List<FileToUpload> filesToUpload) {
            return name();
        }
    };
    State state;
    @Autowired
    FailureStateOperations failureStateOperations;
    @Autowired
    WriteService writeService;


}
