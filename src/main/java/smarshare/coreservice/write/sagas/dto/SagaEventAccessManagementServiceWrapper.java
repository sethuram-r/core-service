package smarshare.coreservice.write.sagas.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import smarshare.coreservice.write.dto.BucketObjectForEvent;

import java.util.List;


public class SagaEventAccessManagementServiceWrapper {

    private String eventId;
    private List<BucketObjectForEvent> objectsForAccessManagementEvent;

    @JsonCreator
    public SagaEventAccessManagementServiceWrapper(@JsonProperty("eventId") String eventId,
                                                   @JsonProperty("objects") List<BucketObjectForEvent> objectsForAccessManagementEvent) {
        this.eventId = eventId;
        this.objectsForAccessManagementEvent = objectsForAccessManagementEvent;
    }


    public String getEventId() {
        return eventId;
    }

    public List<BucketObjectForEvent> getObjectsForAccessManagementEvent() {
        return objectsForAccessManagementEvent;
    }

}
