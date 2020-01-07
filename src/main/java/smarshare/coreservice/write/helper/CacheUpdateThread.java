package smarshare.coreservice.write.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import smarshare.coreservice.cache.model.CacheManager;
import smarshare.coreservice.cache.model.FileToBeCached;
import smarshare.coreservice.write.model.FileToUpload;


@Slf4j
public class CacheUpdateThread implements Runnable {

    public Thread thread;
    @Autowired
    CacheManager cacheManager;
    private FileToUpload fileToUpload;


    public CacheUpdateThread(FileToUpload fileToUpload) {
        thread = new Thread( this, "Cache Update Thread" );
        this.fileToUpload = fileToUpload;

    }

    @Override
    public void run() {

        log.info( "Inside Cache Update..." );

        try {
            cacheManager.updatingCacheEntry( new FileToBeCached( this.fileToUpload.getUploadedFileName(), this.fileToUpload.getUploadedFileContent() ) );
            log.info( "Cache update done For Object :" + this.fileToUpload.getUploadedFileName() );

        } catch (Exception e) {
            log.error( "Exception in Cache Thread" + e.getMessage() );
        }
    }
}


