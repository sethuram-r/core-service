package smarshare.coreservice.write.helper;

import lombok.extern.slf4j.Slf4j;
import smarshare.coreservice.cache.model.CacheManager;


@Slf4j
public class CacheDeleteThread implements Runnable {

    public Thread thread;
    CacheManager cacheManager;
    private String fileToBeDeletedFromCache;

    public CacheDeleteThread(String fileToBeDeletedFromCache, CacheManager cacheManager) {
        thread = new Thread( this, "Cache Delete Thread" );
        this.fileToBeDeletedFromCache = fileToBeDeletedFromCache;
        this.cacheManager = cacheManager;

    }

    @Override
    public void run() {

        log.info( "Inside Cache Delete..." );
        try {
            cacheManager.deletingCacheEntry( this.fileToBeDeletedFromCache );
            log.info( "Caching Done For Object :" + this.fileToBeDeletedFromCache );

        } catch (Exception e) {
            log.error( "Exception in Cache Thread" + e.getMessage() );
        }

    }
}


