package smarshare.coreservice.read.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import smarshare.coreservice.read.model.filestructure.AccessInfo;
import smarshare.coreservice.read.model.filestructure.FileComponent;
import smarshare.coreservice.read.model.filestructure.FolderComponent;

import java.util.List;
import java.util.regex.Pattern;


@Slf4j
@Component
public class BucketObjectsHelper {

    private Pattern fileExtensionRegex = Pattern.compile( "(.*)([a-zA-Z0-9\\s_\\\\.\\-\\(\\):])+(\\..*)$" );


    private FolderComponent fileStructureConverter(List<String> extractedKeys, String bucketName) {
        log.info( "Inside fileStructureConverter" );

        // Forming the root node
        AccessInfo dummyAccess = new AccessInfo( Boolean.TRUE, Boolean.FALSE, Boolean.FALSE );
        FolderComponent root = new FolderComponent( bucketName, dummyAccess, "", "/" );
        FolderComponent previousFolder = root;
        for (String extractedKey : extractedKeys) {

            // file in root folder
            if (fileExtensionRegex.matcher( extractedKey ).matches() && (!extractedKey.contains( "/" ))) {
                root.add( new FileComponent( extractedKey, dummyAccess, "sethuram", extractedKey ) );
            }

            //only folders and files within folders are allowed
            if (extractedKey.endsWith( "/" ) || fileExtensionRegex.matcher( extractedKey ).matches()) {

                //first level of folder
                if (extractedKey.endsWith( "/" ) && (previousFolder.getName().equals( bucketName ) || !extractedKey.contains( previousFolder.getName() + "/" ))) {
                    previousFolder = (FolderComponent) root.add( new FolderComponent( extractedKey.replace( "/", " " ).trim(), dummyAccess, "sethuram", extractedKey ) );
                } else // sub level in folders
                    if (extractedKey.endsWith( "/" ) && extractedKey.contains( previousFolder.getName() + "/" )) {
                        previousFolder = (FolderComponent) previousFolder.add( new FolderComponent( extractedKey.replace( previousFolder.getCompleteName(), " " ).replace( "/", " " ).trim(), dummyAccess, "sethuram", extractedKey ) );
                    } else //file in sub level folders
                        if (fileExtensionRegex.matcher( extractedKey ).matches() && extractedKey.contains( previousFolder.getName() + "/" )) {
                            previousFolder.add( new FileComponent( extractedKey.replace( previousFolder.getCompleteName(), " " ).trim(), dummyAccess, "sethuram", extractedKey ) );
                        }
            } else {
                //file without extensions
                previousFolder.add( new FileComponent( extractedKey.replace( previousFolder.getCompleteName(), " " ).trim(), dummyAccess, "sethuram", extractedKey ) );
            }
        }
        return root;
    }

    public void convertKeysInFileStructureFormat(List<String> extractedKeys, String userName, String bucketName) {
        log.info( "Inside convertKeysInFileStructureFormat" );
        fileStructureConverter(extractedKeys,bucketName);
        // have to include access details in the file structure converter
    }

}

