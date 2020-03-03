package smarshare.coreservice.read.service.helper;


import lombok.extern.slf4j.Slf4j;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.cache.model.DownloadedCacheObject;
import smarshare.coreservice.cache.model.FileToBeCached;

import java.util.Base64;

@Slf4j
public class CacheInsertionThread implements Runnable {

    public Thread thread;


    CacheManager cacheManager;
    private DownloadedCacheObject downloadedCacheObject;

    public CacheInsertionThread(CacheManager cacheManager, DownloadedCacheObject downloadedCacheObject) {
        thread = new Thread( this, "Cache Thread" );
        this.downloadedCacheObject = downloadedCacheObject;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run() {
        log.info( "Inside Caching" );
        try {
            byte[] contentInByteArray = this.downloadedCacheObject.getFileContentInBase64().getBytes();
            cacheManager.createNewCacheEntry(
                    new FileToBeCached( downloadedCacheObject.getBucketName() + "/" + downloadedCacheObject.getObjectName(), Base64.getEncoder().encodeToString( contentInByteArray )
                    ) );
            log.info( "Caching Done For Object :" + downloadedCacheObject.getBucketName() + "/" + downloadedCacheObject.getObjectName() );

        } catch (Exception e) {
            log.error( "Exception in Cache Thread" + e );
        }

    }
}
