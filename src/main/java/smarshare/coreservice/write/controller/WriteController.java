package smarshare.coreservice.write.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.write.model.*;
import smarshare.coreservice.write.service.WriteService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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


    @PostMapping(value = "bucket")
    public Status createBucket(@RequestBody Bucket bucket) {
        log.info( "Inside createBucket" );
        return writeService.createBucketInStorage( bucket );

    }

    @DeleteMapping(value = "bucket")
    public void deleteBucket(@RequestBody Bucket bucket) {
        log.info( "Inside createBucket" );
        writeService.deleteBucketInStorage( bucket );

    }

    @PostMapping(value = "folder/empty")
    public Status createFolder(@RequestParam("folder") Folder folder, @RequestParam("bucketName") String bucketName) {
        log.info( "Inside createFolder" );
//        return writeService.createEmptyFolder( "test/" , "file.server.1" );
        return writeService.createEmptyFolder( folder, bucketName );
    }

    // @RequestParam("file") MultipartFile file

    @DeleteMapping(value = "file")
    public Status deleteFile(@RequestParam("file") File file, @RequestParam("bucketName") String bucketName) {
        log.info( "Inside deleteFile" );
        return writeService.deleteFileInStorage( file, bucketName );
    }

    @DeleteMapping(value = "folder")
    public Status deleteFolder() {
//    public Status deleteFolder(@RequestParam("folder") List<String> folderObjects, @RequestParam("bucketName") String bucketName){
        log.info( "Inside deleteFolder" );
        List<String> stub = new ArrayList<>();
        stub.add( "test/" );
        // lock has to be implemented
        return writeService.deleteFolderInStorage( stub, "file.server.1" );
//        return writeService.deleteFolderInStorage( folderObjects , bucketName );
    }


    @PostMapping(value = "object")
    public void uploadObject(@RequestBody FileToUpload[] filesToUpload) {
        log.info( "Inside uploadFile " );
        // have to spin up a new thread so that its doesn't create a non blocking scenario.
        writeService.uploadObjectToS3( Arrays.asList( filesToUpload ) );
    }






}
