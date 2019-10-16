package smarshare.coreservice.read.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarshare.coreservice.read.model.Bucket;
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
    public List<Bucket> getFilesAndFoldersForIndividualUserForParticularBucket(){
        log.info( "Inside getBucketList" );
        return readService.getFilesAndFoldersByUserAndBucket( "sethuram","file.server.1" );
    }
}
