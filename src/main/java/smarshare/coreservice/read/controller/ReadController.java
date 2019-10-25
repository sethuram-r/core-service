package smarshare.coreservice.read.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.model.S3DownloadObject;
import smarshare.coreservice.read.model.S3DownloadedObject;
import smarshare.coreservice.read.service.ReadService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/",produces = "application/json")
@CrossOrigin(origins = "*")
public class ReadController {

    private ReadService readService;

    @Autowired
    ReadController(ReadService readService){
        this.readService = readService;
    }

    @GetMapping(value = "buckets")
    public List<Bucket> getBucketList(){
        log.info( "Inside getBucketList" );
        return readService.getBucketListFromS3();
    }

    @GetMapping(value = "objects")
    public List<Bucket> listFilesAndFoldersForIndividualUserForParticularBucket() {
        log.info( "Inside listFilesAndFoldersForIndividualUserForParticularBucket" );
        return readService.getFilesAndFoldersListByUserAndBucket( "sethuram", "file.server.1" );
    }

    @GetMapping(value = "file/download")
    // public ResponseEntity<Resource> getFileForIndividualUserForParticularBucket(@RequestParam String objectName, @RequestParam(defaultValue = "motivation.pdf") String fileName, @RequestParam(defaultValue = "file.server.1") String bucketName) {
    public ResponseEntity<Resource> getFileForIndividualUserForParticularBucket(@RequestParam("object") S3DownloadObject S3DownloadObject) { // have to pass object in this format from ui
        log.info( "Inside getFileForIndividualUserForParticularBucket" );
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( "application/octet-stream" ) )
                .header( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + S3DownloadObject.getFileName() + "\"" )
                .body( readService.downloadFile( S3DownloadObject ).getDownloadedObjectResource() );
    }

    @GetMapping(value = "folder/download")
    public ResponseEntity<List<S3DownloadedObject>> getFolderForIndividualUserForParticularBucket(@RequestParam("objects") List<S3DownloadObject> objectsToBeDownloaded) {
        log.info( "Inside getFolderForIndividualUserForParticularBucket" );
        // Stub
//        Map<String, Map<String, String>> fileNameWrapper = new HashMap<>();
//        Map<String, String> s3ObjectDownloadInfo = new HashMap<>();
//        s3ObjectDownloadInfo.put( "motivation.pdf", "file.server.1" );
//        fileNameWrapper.put( "motivation.pdf", s3ObjectDownloadInfo );
        return ResponseEntity.ok().body( readService.downloadFolder( objectsToBeDownloaded ) );
    }

}
