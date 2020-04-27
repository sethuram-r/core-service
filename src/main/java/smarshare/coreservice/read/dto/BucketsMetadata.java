package smarshare.coreservice.read.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


public @Data
class BucketsMetadata {

    private List<BucketMetadata> bucketsMetadata;

    @JsonCreator
    public BucketsMetadata(@JsonProperty("bucketsMetadata") List<BucketMetadata> bucketsMetadata) {
        this.bucketsMetadata = bucketsMetadata;
    }
}
