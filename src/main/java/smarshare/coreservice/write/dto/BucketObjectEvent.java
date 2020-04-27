package smarshare.coreservice.write.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public @Data
class BucketObjectEvent {


    private String bucketName;
    private String objectName;
    private String ownerName;
    private String userName;
    private String status = "In process";


    @JsonCreator
    public BucketObjectEvent(
            @JsonProperty("bucketName") String bucketName,
            @JsonProperty("objectName") String objectName,
            @JsonProperty("ownerName") String ownerName,
            @JsonProperty("userName") String userName,
            @JsonProperty("status") String status
    ) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.ownerName = ownerName;
        this.userName = userName;
        this.status = status;
    }

}
