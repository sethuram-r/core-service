package smarshare.coreservice.write.sagas.dto;

import smarshare.coreservice.write.model.FileToUpload;

import java.util.List;

public class SagaEventWrapper {
    private String eventId;
    private List<FileToUpload> objects;
    private String recentSuccessfulState;

    public SagaEventWrapper(List<FileToUpload> objects, String eventId) {
        this.objects = objects;
        this.eventId = eventId;
    }

    public String getRecentSuccessfulState() {
        return recentSuccessfulState;
    }

    public SagaEventWrapper setRecentSuccessfulState(String recentSuccessfulState) {
        this.recentSuccessfulState = recentSuccessfulState;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public List<FileToUpload> getObjects() {
        return objects;
    }
}
