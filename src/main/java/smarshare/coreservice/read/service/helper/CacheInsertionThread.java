package smarshare.coreservice.read.service.helper;

import com.oracle.tools.packager.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.cache.model.FileToBeCached;
import smarshare.coreservice.read.model.S3DownloadObject;
import smarshare.coreservice.read.model.S3DownloadedObject;

import java.io.IOException;
import java.util.Base64;

@Slf4j
public class CacheInsertionThread implements Runnable {

    public Thread thread;
    @Autowired
    CacheManager cacheManager;
    private S3DownloadObject objectToBeCached;
    private S3DownloadedObject s3DownloadedObject;

    public CacheInsertionThread(S3DownloadObject s3DownloadObject, S3DownloadedObject s3DownloadedObject) {
        thread = new Thread( this, "Cache Thread" );
        this.objectToBeCached = s3DownloadObject;
        this.s3DownloadedObject = s3DownloadedObject;
    }

    @Override
    public void run() {
        log.info( "Inside Caching" );
        try {
            byte[] contentInByteArray = IOUtils.readFully( this.s3DownloadedObject.getDownloadedObjectResource().getFile() );
            cacheManager.createNewCacheEntry(
                    new FileToBeCached( this.objectToBeCached.getObjectName(), Base64.getEncoder().encodeToString( contentInByteArray )
                    ) );
            log.info( "Caching Done For Object :" + this.objectToBeCached.getObjectName() );

        } catch (IOException e) {
            log.error( "Exception in Cache Thread" + e.getMessage() );
        }

    }
}
