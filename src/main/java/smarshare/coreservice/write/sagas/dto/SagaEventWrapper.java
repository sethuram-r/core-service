package smarshare.coreservice.write.sagas.dto;


import smarshare.coreservice.write.model.UploadObject;

import java.util.List;

public class SagaEventWrapper {
    private String eventId;
    private List<UploadObject> objects;
    private String recentSuccessfulState;

    public SagaEventWrapper(List<UploadObject> objects, String eventId) {
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

    public List<UploadObject> getObjects() {
        return objects;
    }
}
