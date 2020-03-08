package smarshare.coreservice.write.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.write.dto.DeleteObjectRequest;
import smarshare.coreservice.write.dto.DeleteObjectsRequest;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.service.BucketObjectService;
import smarshare.coreservice.write.service.BucketService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/", produces = "application/json")
@CrossOrigin(origins = "*")
public class WriteController {

    private BucketObjectService bucketObjectService;
    private BucketService bucketService;


    @Autowired
    WriteController(BucketObjectService bucketObjectService, BucketService bucketService) {
        this.bucketObjectService = bucketObjectService;
        this.bucketService = bucketService;
    }


    @PostMapping(value = "bucket")
    public Boolean createBucket(@RequestBody Bucket bucket) {
        log.info( "Inside createBucket" );
        return bucketService.createBucket( bucket );

    }

    @DeleteMapping(value = "bucket")
    public Boolean deleteBucket(@RequestParam("bucketName") String bucketName) {
        log.info( "Inside createBucket" );
        return bucketService.deleteBucket( bucketName );

    }

    @PostMapping(value = "folder/empty")
    public Boolean createFolder(@RequestBody UploadObject emptyFolder) {
        log.info( "Inside createFolder" );
        return bucketObjectService.createEmptyFolder( emptyFolder );
    }


    //objectName is always complete name without bucketName
    @DeleteMapping(value = "file")
    public Boolean deleteFile(@RequestBody DeleteObjectRequest deleteObjectRequest) {
        log.info( "Inside deleteFile" );
        return bucketObjectService.deleteObject( deleteObjectRequest.getObjectName(),
                deleteObjectRequest.getBucketName(),
                deleteObjectRequest.getOwnerName() );
    }

    @DeleteMapping(value = "folder")
    public Boolean deleteFolder(@RequestBody DeleteObjectsRequest deleteObjectsRequest) {
        log.info( "Inside deleteFolder" );
        return bucketObjectService.deleteFolderInStorage( deleteObjectsRequest );
    }


    @PostMapping(value = "object")
    public Boolean uploadObject(@RequestBody List<UploadObject> filesToUpload) {
        log.info( "Inside uploadFile " );
        return bucketObjectService.UploadObjectThroughSaga( filesToUpload );
    }






}
