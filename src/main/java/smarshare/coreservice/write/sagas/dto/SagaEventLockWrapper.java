package smarshare.coreservice.write.sagas.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import smarshare.coreservice.write.model.lock.S3Object;

import java.util.List;

@JsonRootName("SagaEvent")
public @Data
class SagaEventLockWrapper {
    private String eventId;
    private List<S3Object> objects;
    private String status = "In process";


    @JsonCreator
    public SagaEventLockWrapper(@JsonProperty("eventId") String eventId,
                                @JsonProperty("objects") List<S3Object> objects) {
        this.eventId = eventId;
        this.objects = objects;
    }


}
