package smarshare.coreservice.write.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.cache.model.CacheManager;


@Slf4j
public class CacheDeleteThread implements Runnable {

    public Thread thread;
    @Autowired
    CacheManager cacheManager;
    private String fileToBeDeletedFromCache;

    public CacheDeleteThread(String fileToBeDeletedFromCache) {
        thread = new Thread( this, "Cache Delete Thread" );
        this.fileToBeDeletedFromCache = fileToBeDeletedFromCache;

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


