package smarshare.coreservice.write.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.service.WriteService;


@Slf4j
@RestController
@RequestMapping(path = "/", produces = "application/json")
@CrossOrigin(origins = "*")
public class WriteController {

    private WriteService writeService;

    @Autowired
    WriteController(WriteService writeService) {
        this.writeService = writeService;
    }


    @PostMapping(value = "/bucket/create")
    public void createBucket(@RequestBody Bucket bucket) {
        log.info( "Inside createBucket" );
        writeService.createBucketInStorage( bucket );

    }
}
