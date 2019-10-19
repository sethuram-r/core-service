package smarshare.coreservice.read.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.read.model.Bucket;
import smarshare.coreservice.read.service.ReadService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @GetMapping(value = "file")
    public ResponseEntity<Resource> getFileForIndividualUserForParticularBucket(@RequestParam String objectName, @RequestParam(defaultValue = "motivation.pdf") String fileName, @RequestParam(defaultValue = "file.server.1") String bucketName) {
        log.info( "Inside getFileForIndividualUserForParticularBucket" );
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( "application/octet-stream" ) )
                .header( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"" )
                .body( readService.downloadFile( objectName, fileName, bucketName ).get( fileName ) );
    }

    @GetMapping(value = "folder")
    public ResponseEntity<List<Map<String, Resource>>> getFolderForIndividualUserForParticularBucket() {
        log.info( "Inside getFolderForIndividualUserForParticularBucket" );
        // Stub
        Map<String, Map<String, String>> fileNameWrapper = new HashMap<>();
        Map<String, String> s3ObjectDownloadInfo = new HashMap<>();
        s3ObjectDownloadInfo.put( "motivation.pdf", "file.server.1" );
        fileNameWrapper.put( "motivation.pdf", s3ObjectDownloadInfo );
        return ResponseEntity.ok().body( readService.downloadFolder( fileNameWrapper ) );
    }

}
