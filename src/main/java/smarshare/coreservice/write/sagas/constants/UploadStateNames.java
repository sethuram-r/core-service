package smarshare.coreservice.write.sagas.constants;

public enum UploadStateNames {

    UPLOAD_REQUEST_ACCEPTED( "uploadRequestAccepted" ),
    LOCK_EVENT_SENT_TO_KAFKA( "lockEventSentToKafka" ),
    LOCK_EVENT_RESULT_FROM_KAFKA( "lockEventResultFromKafka" ),
    S3_UPLOAD_AND_CACHE_TASK( "s3UploadAndCache" ),
    S3_DELETE_UPLOAD_AND_CACHE_TASK( "s3DeleteUploadAndCache" ),
    ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_TO_KAFKA( "accessManagementEntryCreate" ),
    ACCESS_MANAGEMENT_ENTRY_CREATE_EVENT_RESULT_FROM_KAFKA( "accessManagementEntryCreateResult" ),
    UNLOCK_EVENT_TO_KAFKA_AFTER_SUCCESS( "unlockEventAfterSuccess" ),
    UNLOCK_EVENT_RESULT_FROM_KAFKA_AFTER_SUCCESS( "unlockEventResultAfterSuccess" ),
    UPLOAD_REQUEST_REJECTED( "uploadRequestRejected" ),
    COMPLETED( "completed" ),
    UNLOCK_EVENT_TO_KAFKA_AFTER_FAILURE( "unlockEventAfterFailure" ),
    UNLOCK_EVENT_RESULT_FROM_KAFKA_AFTER_FAILURE( "unlockEventResultAfterFailure" ),
    ERROR( "error" );

    private String stateName;

    UploadStateNames(String stateName) {
        this.stateName = stateName;
    }

    public String valueOf() {
        return this.stateName;
    }

}
