package smarshare.coreservice.write.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public @Data
class AccessInfo {

    private Boolean read;
    private Boolean write;
    private Boolean delete;

}
