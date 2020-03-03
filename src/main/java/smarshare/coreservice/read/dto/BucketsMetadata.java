package smarshare.coreservice.read.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
public @Data
class BucketsMetadata {

    private List<BucketMetadata> bucketsMetadata;
}
