package smarshare.coreservice.write.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.cache.model.FileToBeCached;
import smarshare.coreservice.write.model.UploadObject;


@Slf4j
public class CacheUpdateThread implements Runnable {

    public Thread thread;
    @Autowired
    CacheManager cacheManager;
    private UploadObject uploadObject;


    public CacheUpdateThread(UploadObject uploadObject) {
        thread = new Thread( this, "Cache Update Thread" );
        this.uploadObject = uploadObject;
    }

    @Override
    public void run() {

        log.info( "Inside Cache Update..." );

        try {
            cacheManager.updatingCacheEntry( new FileToBeCached( this.uploadObject.getBucketName() + "/" + uploadObject.getObjectName(), this.uploadObject.getContent() ) );
            log.info( "Cache update done For Object :" + this.uploadObject.getBucketName() + "/" + uploadObject.getObjectName() );

        } catch (Exception e) {
            log.error( "Exception in Cache Thread" + e.getMessage() );
        }
    }
}


