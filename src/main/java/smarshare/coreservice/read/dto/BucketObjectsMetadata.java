package smarshare.coreservice.read.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public @Data
class BucketObjectsMetadata {

    private List<BucketObjectMetadata> bucketObjectsMetadata;
}
