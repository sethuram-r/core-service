package smarshare.coreservice.write.sagas.upload;

import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.write.sagas.constants.UploadStateNames;


public class UploadStates {


    @Autowired
    UploadStateTasks uploadStateTasks;
    private StateTemplate uploadRequestAccepted;
    private StateTemplate uploadRequestRejected;
    private StateTemplate lockEventSentToKafka;
    private StateTemplate lockEventResultFromKafka;
    private StateTemplate s3UploadAndCache;
    private StateTemplate accessManagementEntryCreate;
    private StateTemplate accessManagementEntryCreateResult;
    private StateTemplate unlockEventAfterSuccess;
    private StateTemplate unlockEventResultAfterSuccess;
    private StateTemplate unlockEventAfterFailure;
    private StateTemplate unlockEventResultAfterFailure;
    private StateTemplate s3DeleteUploadAndCache;
    private StateTemplate completed;
    private StateTemplate error;


    UploadStates() {

        this.uploadRequestAccepted = new StateTemplate.Builder( false, UploadStateNames.UPLOAD_REQUEST_ACCEPTED )
                .successState( false, UploadStateNames.LOCK_EVENT_SENT_TO_KAFKA )
                .failureState( true, UploadStateNames.UPLOAD_REQUEST_REJECTED )
                .build();

        this.uploadRequestRejected = uploadRequestAccepted.getFailureState();

        // terminal builder is used to override the default taskToBeDoneInThisState

//        uploadRequestRejected = uploadRequestRejected.getTerminalStateBuilder()
//                .taskToBeDoneInThisState( StringUtils::uncapitalize ).build();


        this.lockEventSentToKafka = uploadRequestAccepted.getSuccessStateBuilder()
                .successState( false, UploadStateNames.LOCK_EVENT_RESULT_FROM_KAFKA )
                .failureState( uploadRequestRejected )
                .taskToBeDoneInThisState( uploadStateTasks::lockEventToKafka )
                .build();

        this.lockEventResultFromKafka = lockEventSentToKafka.getSuccessStateBuilder()
                .successState( false, UploadStateNames.S3_UPLOAD_AND_CACHE_TASK )
                .failureState( uploadRequestRejected )
                .taskToBeDoneInThisState( uploadStateTasks::consumeLockEventsFromLockServer )
                .build();

        this.s3UploadAndCache = lockEventResultFromKafka.getSuccessStateBuilder()
                .successState( false, UploadStateNames.ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_TO_KAFKA )
                .failureState( false, UploadStateNames.UNLOCK_EVENT_TO_KAFKA_AFTER_FAILURE )
                .taskToBeDoneInThisState( uploadStateTasks::uploadToS3AndRefreshCache )
                .build();


        this.unlockEventAfterFailure = s3UploadAndCache.getFailureStateBuilder()
                .successState( false, UploadStateNames.UNLOCK_EVENT_RESULT_FROM_KAFKA_AFTER_FAILURE )
                .failureState( true, UploadStateNames.ERROR )
                .taskToBeDoneInThisState( uploadStateTasks::unLockEventToKafka )
                .build();

        this.error = unlockEventAfterFailure.getFailureState();

        this.unlockEventResultAfterFailure = unlockEventAfterFailure.getSuccessStateBuilder()
                .successState( uploadRequestRejected )
                .failureState( error )
                .taskToBeDoneInThisState( uploadStateTasks::consumeUnLockEventsFromLockServer )
                .build();

        this.accessManagementEntryCreate = s3UploadAndCache.getSuccessStateBuilder()
                .successState( false, UploadStateNames.ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_RESULT_FROM_KAFKA )
                .failureState( false, UploadStateNames.S3_DELETE_UPLOAD_AND_CACHE_TASK )
                .taskToBeDoneInThisState( uploadStateTasks::accessManagementServiceCreateEntryEventToKafka )
                .build();

        this.s3DeleteUploadAndCache = accessManagementEntryCreate.getFailureStateBuilder()
                .successState( unlockEventAfterFailure )
                .failureState( error )
                .taskToBeDoneInThisState( uploadStateTasks::deleteS3UploadAndCache )
                .build();

        this.accessManagementEntryCreateResult = accessManagementEntryCreate.getSuccessStateBuilder()
                .successState( false, UploadStateNames.UNLOCK_EVENT_TO_KAFKA_AFTER_SUCCESS )
                .failureState( s3DeleteUploadAndCache )
                .taskToBeDoneInThisState( uploadStateTasks::consumeCreateEventsFromAccessManagementServer )
                .build();

        // bypass state

        this.unlockEventAfterSuccess = accessManagementEntryCreateResult.getSuccessStateBuilder()
                .successState( false, UploadStateNames.UNLOCK_EVENT_RESULT_FROM_KAFKA_AFTER_SUCCESS )
                .failureState( true, UploadStateNames.COMPLETED )
                .taskToBeDoneInThisState( uploadStateTasks::unLockEventToKafka )
                .build();

        this.completed = unlockEventAfterSuccess.getFailureState();

        this.unlockEventResultAfterSuccess = unlockEventAfterSuccess.getSuccessStateBuilder()
                .successState( completed )
                .failureState( completed )
                .taskToBeDoneInThisState( uploadStateTasks::consumeUnLockEventsFromLockServer )
                .build();

    }

    public StateTemplate getUploadRequestAccepted() {
        return uploadRequestAccepted;
    }

    public StateTemplate getUploadRequestRejected() {
        return uploadRequestRejected;
    }


    public StateTemplate getCompleted() {
        return completed;
    }

    public StateTemplate getInitialStateForUploadOrchestrator() {
        return this.uploadRequestAccepted;
    }


    public StateTemplate getLockEventSentToKafka() {
        return lockEventSentToKafka;
    }

    public StateTemplate getLockEventResultFromKafka() {
        return lockEventResultFromKafka;
    }

    public StateTemplate getS3UploadAndCache() {
        return s3UploadAndCache;
    }

    public StateTemplate getAccessManagementEntryCreate() {
        return accessManagementEntryCreate;
    }

    public StateTemplate getAccessManagementEntryCreateResult() {
        return accessManagementEntryCreateResult;
    }

    public StateTemplate getUnlockEventAfterSuccess() {
        return unlockEventAfterSuccess;
    }

    public StateTemplate getUnlockEventResultAfterSuccess() {
        return unlockEventResultAfterSuccess;
    }

    public StateTemplate getUnlockEventAfterFailure() {
        return unlockEventAfterFailure;
    }

    public StateTemplate getUnlockEventResultAfterFailure() {
        return unlockEventResultAfterFailure;
    }

    public StateTemplate getS3DeleteUploadAndCache() {
        return s3DeleteUploadAndCache;
    }

    public StateTemplate getError() {
        return error;
    }

    public UploadStateTasks getUploadStateTasks() {
        return uploadStateTasks;
    }

    public StateTemplate getUploadStateByName(UploadStateNames name) {

        switch (name) {
            case UPLOAD_REQUEST_ACCEPTED:
                return getUploadRequestAccepted();
            case LOCK_EVENT_SENT_TO_KAFKA:
                return getLockEventSentToKafka();
            case LOCK_EVENT_RESULT_FROM_KAFKA:
                return getLockEventResultFromKafka();
            case S3_UPLOAD_AND_CACHE_TASK:
                return getS3UploadAndCache();
            case S3_DELETE_UPLOAD_AND_CACHE_TASK:
                return getS3DeleteUploadAndCache();
            case ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_TO_KAFKA:
                return getAccessManagementEntryCreate();
            case ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_RESULT_FROM_KAFKA:
                return getAccessManagementEntryCreateResult();
            case UNLOCK_EVENT_TO_KAFKA_AFTER_SUCCESS:
                return getUnlockEventAfterSuccess();
            case UNLOCK_EVENT_RESULT_FROM_KAFKA_AFTER_SUCCESS:
                return getUnlockEventResultAfterSuccess();
            case UNLOCK_EVENT_TO_KAFKA_AFTER_FAILURE:
                return getUnlockEventAfterFailure();
            case UNLOCK_EVENT_RESULT_FROM_KAFKA_AFTER_FAILURE:
                return getUnlockEventResultAfterFailure();
            case COMPLETED:
                return getCompleted();
            case UPLOAD_REQUEST_REJECTED:
                return getUploadRequestRejected();
            default:
                return getError();
        }
    }


}
