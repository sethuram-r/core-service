package smarshare.coreservice.read.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import smarshare.coreservice.read.dto.ObjectMetadata;
import smarshare.coreservice.read.model.filestructure.AccessInfo;
import smarshare.coreservice.read.model.filestructure.FileComponent;
import smarshare.coreservice.read.model.filestructure.FolderComponent;

import java.util.Map;
import java.util.regex.Pattern;


@Slf4j
@Component
public class BucketObjectsHelper {

    private Pattern fileExtensionRegex = Pattern.compile( "(.*)([a-zA-Z0-9\\s_\\\\.\\-\\(\\):])+(\\..*)$" );


    private FolderComponent fileStructureConverter(Map<String, String> extractedKeys, String bucketName, Map<String, ObjectMetadata> objectMetadata) {
        log.info( "Inside fileStructureConverter" );

        // Forming the root node

        FolderComponent root = new FolderComponent( bucketName, null, "", "/" );
        FolderComponent previousFolder = root;
        for (Map.Entry<String, String> extractedKey : extractedKeys.entrySet()) {

            AccessInfo currentKeyAccessInfo = null;
            String owner = "";

            // fetching access details needed for forming the tree

            if (objectMetadata.containsKey( extractedKey.getKey() )) {
                ObjectMetadata currentObjectMetadata = objectMetadata.get( extractedKey.getKey() );
                if (null != currentObjectMetadata.getAccessingUserInfo())
                    currentKeyAccessInfo = new AccessInfo( currentObjectMetadata.getAccessingUserInfo() );
                owner = currentObjectMetadata.getOwnerName();
            }

            // file in root folder
            if (fileExtensionRegex.matcher( extractedKey.getKey() ).matches() && (!extractedKey.getKey().contains( "/" ))) {
                root.add( new FileComponent( extractedKey.getKey(), currentKeyAccessInfo, owner, extractedKey.getKey(), extractedKey.getValue() ) );
            }

            //only folders and files within folders are allowed
            if (extractedKey.getKey().endsWith( "/" ) || fileExtensionRegex.matcher( extractedKey.getKey() ).matches()) {

                //first level of folder
                if (extractedKey.getKey().endsWith( "/" ) && (previousFolder.getName().equals( bucketName ) || !extractedKey.getKey().contains( previousFolder.getName() + "/" ))) {
                    previousFolder = (FolderComponent) root.add( new FolderComponent( extractedKey.getKey().replace( "/", " " ).trim(), currentKeyAccessInfo, owner, extractedKey.getKey() ) );
                } else // sub level in folders
                    if (extractedKey.getKey().endsWith( "/" ) && extractedKey.getKey().contains( previousFolder.getName() + "/" )) {
                        previousFolder = (FolderComponent) previousFolder.add( new FolderComponent( extractedKey.getKey().replace( previousFolder.getCompleteName(), " " ).replace( "/", " " ).trim(), currentKeyAccessInfo, owner, extractedKey.getKey() ) );
                    } else //file in sub level folders
                        if (fileExtensionRegex.matcher( extractedKey.getKey() ).matches() && extractedKey.getKey().contains( previousFolder.getName() + "/" )) {
                            previousFolder.add( new FileComponent( extractedKey.getKey().replace( previousFolder.getCompleteName(), " " ).trim(), currentKeyAccessInfo, owner, extractedKey.getKey(), extractedKey.getValue() ) );
                        }
            } else {
                //file without extensions
                previousFolder.add( new FileComponent( extractedKey.getKey().replace( previousFolder.getCompleteName(), " " ).trim(), currentKeyAccessInfo, owner, extractedKey.getKey(), extractedKey.getValue() ) );
            }
        }
        return root;
    }

    public FolderComponent convertKeysInFileStructureFormat(Map<String, String> extractedKeys, String bucketName, Map<String, ObjectMetadata> objectMetadata) {
        log.info( "Inside convertKeysInFileStructureFormat" );
        return fileStructureConverter( extractedKeys, bucketName, objectMetadata );
    }

}

