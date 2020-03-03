package smarshare.coreservice.cache;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smarshare.coreservice.cache.model.FileToBeCached;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Comparator;

@Slf4j
@Service
public class FileDirectoryManger {

    private String FileDirectory = "Cache/";

    @PostConstruct
    private void instantiateCacheStorage() {
        log.info( "Initializing Cache Storage" );
        try {

            if (Files.exists( Paths.get( "Cache/" ) )) {
                Files.walk( Paths.get( "Cache/" ) )
                        .sorted( Comparator.reverseOrder() )
                        .map( Path::toFile )
                        .forEach( File::delete );
            }
        } catch (IOException e) {
            log.error( "Error While instantiateCacheStorage " + e );
        }
    }

    private InputStream getBase64DecodedFileContent(String base64EncodedContent) {
        log.info( "Inside getBase64DecodedFileContent method " );
        byte[] base64DecodedContentInByteArray = Base64.getDecoder().decode( base64EncodedContent );
        return new ByteArrayInputStream( base64DecodedContentInByteArray );
    }

    private Path getPathForGivenFileName(String fileName) {
        return Paths.get( FileDirectory + fileName );
    }

    public boolean createFileInCache(FileToBeCached fileToBeCached) {
        log.info( "Inside createFileInCache " );
        Path completeFileNameWithDirectoryPath = getPathForGivenFileName( fileToBeCached.getFileName() );
        try {
            if (!Files.exists( completeFileNameWithDirectoryPath ))
                Files.createDirectories( completeFileNameWithDirectoryPath.getParent() );
            OutputStream cachedFile = new BufferedOutputStream( Files.newOutputStream( completeFileNameWithDirectoryPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE ) );
            int currentPosition;
            int endPositionOfFile = -1;
            InputStream base64DecodedContent = getBase64DecodedFileContent( fileToBeCached.getFileContentInBase64() );
            do {
                currentPosition = base64DecodedContent.read();
                if (currentPosition != endPositionOfFile) {
                    cachedFile.write( (char) currentPosition );
                }
            } while (currentPosition != endPositionOfFile);
            cachedFile.close();
            return Boolean.TRUE;

        } catch (Exception exception) {
            log.error( exception.getMessage() );
            return Boolean.FALSE;
        }
    }

    private boolean deleteFileIfExistsInCache(Path CachedFilePath) {
        log.info( "Inside deleteFileIfExistsInCache" );
        try {
            return Files.deleteIfExists( CachedFilePath );
        } catch (Exception exception) {
            log.error( exception.getMessage() );
            return Boolean.FALSE;
        }
    }

    public boolean updateFileInCache(FileToBeCached fileToBeCached) {
        log.info( "Inside updateFileInCache " );
        deleteFileIfExistsInCache( getPathForGivenFileName( fileToBeCached.getFileName() ) );
        return createFileInCache( fileToBeCached );
    }

    public boolean deleteFileInCache(String CachedFile) {
        log.info( "Inside deleteFileInCache" );
        return deleteFileIfExistsInCache( getPathForGivenFileName( CachedFile ) );
    }

    public FileToBeCached retrieveCachedFile(String fileName) {
        log.info( "Inside retrieveCachedFile" );
        try {
            Path completeFileNameWithDirectoryPath = getPathForGivenFileName( fileName );

            if (Files.exists( completeFileNameWithDirectoryPath )) {
                byte[] contentInByteArray = Files.readAllBytes( completeFileNameWithDirectoryPath.toFile().toPath() );
                return new FileToBeCached( fileName, Base64.getEncoder().encodeToString( contentInByteArray ) );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
