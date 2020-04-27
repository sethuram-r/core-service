package smarshare.coreservice.write.dto;

import lombok.Data;

public @Data
class CustomResponse {

    private Boolean operationResult;
    private String errorMessage;
}
