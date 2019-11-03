package smarshare.coreservice.read.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import smarshare.coreservice.read.dto.ObjectMetadata;
import smarshare.coreservice.read.model.filestructure.AccessInfo;
import smarshare.coreservice.read.model.filestructure.FileComponent;
import smarshare.coreservice.read.model.filestructure.FolderComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;


@Slf4j
@Component
public class BucketObjectsHelper {

    private Pattern fileExtensionRegex = Pattern.compile( "(.*)([a-zA-Z0-9\\s_\\\\.\\-\\(\\):])+(\\..*)$" );


    private FolderComponent fileStructureConverter(List<String> extractedKeys, String bucketName, List<Map<String, ObjectMetadata>> objectMetadata) {
        log.info( "Inside fileStructureConverter" );

        // Forming the root node
        AccessInfo currentKeyAccessInfo = null;
        String owner = "";
        FolderComponent root = new FolderComponent( bucketName, currentKeyAccessInfo, owner, "/" );
        FolderComponent previousFolder = root;
        for (String extractedKey : extractedKeys) {

            // fetching access details needed for forming the tree
            Optional<Map<String, ObjectMetadata>> objectMetadataForGivenKey =
                    objectMetadata.stream().filter( stringObjectMetadataMap -> stringObjectMetadataMap.containsKey( extractedKey ) )
                            .findFirst();
            if (objectMetadataForGivenKey.isPresent()) {
                ObjectMetadata currentObjectMetadata = objectMetadataForGivenKey.get().get( extractedKey );
                if (null != currentObjectMetadata.getAccessingUserInfo())
                    currentKeyAccessInfo = new AccessInfo( currentObjectMetadata.getAccessingUserInfo() );
                owner = currentObjectMetadata.getOwnerName();
            }
            // file in root folder
            if (fileExtensionRegex.matcher( extractedKey ).matches() && (!extractedKey.contains( "/" ))) {
                root.add( new FileComponent( extractedKey, currentKeyAccessInfo, owner, extractedKey ) );
            }

            //only folders and files within folders are allowed
            if (extractedKey.endsWith( "/" ) || fileExtensionRegex.matcher( extractedKey ).matches()) {

                //first level of folder
                if (extractedKey.endsWith( "/" ) && (previousFolder.getName().equals( bucketName ) || !extractedKey.contains( previousFolder.getName() + "/" ))) {
                    previousFolder = (FolderComponent) root.add( new FolderComponent( extractedKey.replace( "/", " " ).trim(), currentKeyAccessInfo, owner, extractedKey ) );
                } else // sub level in folders
                    if (extractedKey.endsWith( "/" ) && extractedKey.contains( previousFolder.getName() + "/" )) {
                        previousFolder = (FolderComponent) previousFolder.add( new FolderComponent( extractedKey.replace( previousFolder.getCompleteName(), " " ).replace( "/", " " ).trim(), currentKeyAccessInfo, owner, extractedKey ) );
                    } else //file in sub level folders
                        if (fileExtensionRegex.matcher( extractedKey ).matches() && extractedKey.contains( previousFolder.getName() + "/" )) {
                            previousFolder.add( new FileComponent( extractedKey.replace( previousFolder.getCompleteName(), " " ).trim(), currentKeyAccessInfo, owner, extractedKey ) );
                        }
            } else {
                //file without extensions
                previousFolder.add( new FileComponent( extractedKey.replace( previousFolder.getCompleteName(), " " ).trim(), currentKeyAccessInfo, owner, extractedKey ) );
            }
        }
        return root;
    }

    public FolderComponent convertKeysInFileStructureFormat(List<String> extractedKeys, String bucketName, List<Map<String, ObjectMetadata>> objectMetadata) {
        log.info( "Inside convertKeysInFileStructureFormat" );
        return fileStructureConverter( extractedKeys, bucketName, objectMetadata );
    }

}

