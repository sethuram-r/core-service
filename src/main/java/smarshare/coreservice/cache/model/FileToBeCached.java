package smarshare.coreservice.cache.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
public @Data
class FileToBeCached {

    private String fileName;
    private String fileContentInBase64;
}
