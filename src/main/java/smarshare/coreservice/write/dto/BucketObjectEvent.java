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
    private int ownerId;
    private int userId;
    private String status = "In process";


    @JsonCreator
    public BucketObjectEvent(
            @JsonProperty("bucketName") String bucketName,
            @JsonProperty("objectName") String objectName,
            @JsonProperty("ownerName") String ownerName,
            @JsonProperty("userName") String userName,
            @JsonProperty("ownerId") int ownerId,
            @JsonProperty("userId") int userId,
            @JsonProperty("status") String status


    ) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.ownerName = ownerName;
        this.userName = userName;
        this.ownerId = ownerId;
        this.userId = userId;
        this.status = status;

    }

}
