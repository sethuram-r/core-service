package smarshare.coreservice.write.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.write.dto.CustomResponse;
import smarshare.coreservice.write.dto.DeleteObjectsRequest;
import smarshare.coreservice.write.model.Bucket;
import smarshare.coreservice.write.model.UploadObject;
import smarshare.coreservice.write.service.BucketObjectService;
import smarshare.coreservice.write.service.BucketService;
import smarshare.coreservice.write.service.SagaBucketObjectUploadService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/", produces = "application/json")
@CrossOrigin(origins = "*")
public class WriteController {

    private final BucketObjectService bucketObjectService;
    private final BucketService bucketService;
    private final SagaBucketObjectUploadService sagaBucketObjectUploadService;


    @Autowired
    WriteController(BucketObjectService bucketObjectService, BucketService bucketService, SagaBucketObjectUploadService sagaBucketObjectUploadService) {
        this.bucketObjectService = bucketObjectService;
        this.bucketService = bucketService;
        this.sagaBucketObjectUploadService = sagaBucketObjectUploadService;
    }


    @PostMapping(value = "bucket")
    public ResponseEntity createBucket(@RequestBody Bucket bucket) {
        log.info( "Inside createBucket" );
        final CustomResponse createBucketResult = bucketService.createBucket( bucket );

        if (Boolean.FALSE.equals( createBucketResult.getOperationResult() ) && null != createBucketResult.getErrorMessage()) {
            return new ResponseEntity<>( HttpStatus.PRECONDITION_FAILED );
        } else return ResponseEntity.ok( true );
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


    @DeleteMapping(value = "file")
    public Boolean deleteFile(@RequestParam("objectName") String objectName,
                              @RequestParam("bucketName") String bucketName,
                              @RequestParam("ownerId") int ownerId) {
        log.info( "Inside deleteFile" );
        return bucketObjectService.deleteObject( objectName, bucketName, ownerId );
    }

    @DeleteMapping(value = "folder")
    public Boolean deleteFolder(@RequestBody DeleteObjectsRequest deleteObjectsRequest) {
        log.info( "Inside deleteFolder" );
        return bucketObjectService.deleteFolderInStorage( deleteObjectsRequest );
    }


    @PostMapping(value = "object")
    public ResponseEntity<Boolean> uploadObject(@RequestBody List<UploadObject> filesToUpload) {
        log.info( "Inside uploadFile " );
        return sagaBucketObjectUploadService.UploadObjectThroughSaga( filesToUpload );
    }






}
