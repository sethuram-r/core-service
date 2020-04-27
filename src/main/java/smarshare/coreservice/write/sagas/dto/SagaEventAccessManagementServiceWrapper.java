package smarshare.coreservice.write.sagas.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import smarshare.coreservice.write.dto.BucketObjectEvent;

import java.util.List;

@JsonRootName("SagaEvent")
public @Data
class SagaEventAccessManagementServiceWrapper {

    private String eventId;
    private List<BucketObjectEvent> objects;
    private String status = "In process";

    @JsonCreator
    public SagaEventAccessManagementServiceWrapper(@JsonProperty("eventId") String eventId,
                                                   @JsonProperty("objects") List<BucketObjectEvent> objects

    ) {
        this.eventId = eventId;
        this.objects = objects;
    }


    public String getEventId() {
        return eventId;
    }

    public List<BucketObjectEvent> getObjects() {
        return objects;
    }

    public String getStatus() {
        return status;
    }
}
