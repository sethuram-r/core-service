package smarshare.coreservice.write.model.lock;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


public @Data
class S3Object {
    private String objectName;

    @JsonCreator
    public S3Object(@JsonProperty("objectName") String objectName) {
        this.objectName = objectName;
    }
}
