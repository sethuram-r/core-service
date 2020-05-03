package smarshare.coreservice.read.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarshare.coreservice.read.dto.DownloadFolderRequest;
import smarshare.coreservice.read.service.ReadService;


@Slf4j
@RestController
@RequestMapping(path = "/",produces = "application/json")
@CrossOrigin(origins = "*")
public class ReadController {

    private ReadService readService;

    @Autowired
    ReadController(ReadService readService) {
        this.readService = readService;
    }

    @GetMapping(value = "buckets")
    public ResponseEntity getBucketList(@RequestParam("userName") String userName, @RequestParam("email") String email) {
        log.info( "Inside getBucketList" );
        return ResponseEntity.ok().body( readService.getBucketsByUserNameAndEmail( userName, email ) );
    }

    @GetMapping(value = "objects")
    public String listFilesAndFoldersForIndividualUserForParticularBucket(@RequestParam("userId") int userId, @RequestParam("bucketName") String bucketName) {
        log.info( "Inside listFilesAndFoldersForIndividualUserForParticularBucket" );
        return readService.getFilesAndFoldersByUserIdAndBucketName( userId, bucketName );
    }

    @GetMapping(value = "file/download")
    public ResponseEntity<Resource> getFileByObjectNameAndBucketName(
            @RequestParam("fileName") String fileName,
            @RequestParam("objectName") String objectName,
            @RequestParam("bucketName") String bucketName
    ) {
        log.info( "Inside getFileByObjectNameAndBucketName" );
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( "application/octet-stream" ) )
                .header( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"" )
                .header( HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION )
                .body( readService.downloadFile( objectName, bucketName ).getDownloadedObjectResource() );
    }

    @PostMapping(value = "folder/download") // pass only files
    public ResponseEntity getFolderForIndividualUserForParticularBucket(@RequestBody DownloadFolderRequest objectsToBeDownloaded) {
        log.info( "Inside getFolderForIndividualUserForParticularBucket" );
        return ResponseEntity.ok().body( readService.downloadFolder( objectsToBeDownloaded ) );
    }

}
