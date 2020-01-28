package smarshare.coreservice.write.sagas.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import smarshare.coreservice.write.model.lock.S3Object;

import java.util.List;

public class SagaEventLockWrapper {
    private String eventId;
    private List<S3Object> objects;


    @JsonCreator
    public SagaEventLockWrapper(@JsonProperty("eventId") String eventId,
                                @JsonProperty("objects") List<S3Object> objects) {
        this.eventId = eventId;
        this.objects = objects;
    }

    public String getEventId() {
        return eventId;
    }

    public List<S3Object> getObjects() {
        return objects;
    }

    @Override
    public String toString() {
        return "SagaEventLockWrapper{" +
                "eventId='" + eventId + '\'' +
                ", objects=" + objects +
                '}';
    }
}
