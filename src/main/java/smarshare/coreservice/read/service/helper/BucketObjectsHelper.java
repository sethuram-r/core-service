package smarshare.coreservice.read.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import smarshare.coreservice.read.dto.ObjectMetadata;
import smarshare.coreservice.read.model.filestructure.AccessInfo;
import smarshare.coreservice.read.model.filestructure.FileComponent;
import smarshare.coreservice.read.model.filestructure.FolderComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@Slf4j
@Component
public class BucketObjectsHelper {

    private Pattern fileExtensionRegex = Pattern.compile( "(.*)([a-zA-Z0-9\\s_\\\\.\\-\\(\\):])+(\\..*)$" );

    private Boolean checkPreviousFolderInFile(String previousFolderName, String currentObjectName) {
        List<String> previousFolderSplits = Arrays.asList( previousFolderName.split( "/" ) );
        List<String> currentFolderSplits = Arrays.asList( currentObjectName.split( "/" ) );
        return previousFolderSplits.get( previousFolderSplits.size() - 1 ).equals( currentFolderSplits.get( currentFolderSplits.size() - 2 ) );
    }


    private FolderComponent fileStructureConverter(Map<String, String> extractedKeys, String bucketName, Map<String, ObjectMetadata> objectMetadata) {
        log.info( "Inside fileStructureConverter" );


        // Forming the root node

        FolderComponent root = new FolderComponent( bucketName, null, "", 0, "/", null );
        FolderComponent previousFolder = root;
        for (Map.Entry<String, String> extractedKey : extractedKeys.entrySet()) {


            AccessInfo currentKeyAccessInfo = null;
            String owner = "";
            int ownerId = 0;

            // fetching access details needed for forming the tree

            if (objectMetadata.containsKey( extractedKey.getKey() )) {
                ObjectMetadata currentObjectMetadata = objectMetadata.get( extractedKey.getKey() );
                if (null != currentObjectMetadata.getAccessingUserInfo())
                    currentKeyAccessInfo = new AccessInfo( currentObjectMetadata.getAccessingUserInfo() );
                owner = currentObjectMetadata.getOwnerName();
                ownerId = currentObjectMetadata.getOwnerId();
            }

            // file in root folder
            if (fileExtensionRegex.matcher( extractedKey.getKey() ).matches() && (!extractedKey.getKey().contains( "/" ))) {
                root.add( new FileComponent( extractedKey.getKey(), currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), extractedKey.getValue() ) );
            }

            //only folders and files within folders are allowed
            if (extractedKey.getKey().endsWith( "/" ) || fileExtensionRegex.matcher( extractedKey.getKey() ).matches()) {
                //first level of folder
                if (extractedKey.getKey().endsWith( "/" ) && (previousFolder.getName().equals( bucketName ) || (!extractedKey.getKey().contains( previousFolder.getCompleteName() ) && (!extractedKey.getKey().endsWith( "/" ) && !previousFolder.getCompleteName().endsWith( "/" ))))) {
                    previousFolder = (FolderComponent) root.add( new FolderComponent( extractedKey.getKey().replace( "/", " " ).trim(), currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), root ) );
                } else // sub level in folders
                    if (extractedKey.getKey().endsWith( "/" ) && extractedKey.getKey().contains( previousFolder.getName() + "/" ) || (extractedKey.getKey().endsWith( "/" ) && previousFolder.getName().endsWith( "/" ))) {
                        previousFolder = (FolderComponent) previousFolder.add( new FolderComponent( extractedKey.getKey().replace( previousFolder.getCompleteName(), " " ).replace( "/", " " ).trim(), currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), previousFolder ) );
                    } else {
                        //file in sub level folders
                        if (fileExtensionRegex.matcher( extractedKey.getKey() ).matches()) {
                            if (checkPreviousFolderInFile( previousFolder.getCompleteName(), extractedKey.getKey() )) {
                                previousFolder.add( new FileComponent( extractedKey.getKey().replace( previousFolder.getCompleteName(), " " ).trim(), currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), extractedKey.getValue() ) );
                            } else {
                                String[] splitKey = extractedKey.getKey().split( "/" );
                                previousFolder.getParent().add( new FileComponent( splitKey[splitKey.length - 1], currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), extractedKey.getValue() ) );

                            }
                        }
                        // folder - folder sibling
                        if ((extractedKey.getKey().endsWith( "/" ) && previousFolder.getCompleteName().endsWith( "/" ))) {
                            previousFolder = (FolderComponent) previousFolder.getParent().add( new FolderComponent( extractedKey.getKey().replace( previousFolder.getParent().getCompleteName(), " " ).replace( "/", " " ).trim(), currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), previousFolder.getParent() ) );
                        }
                    }
            } else {
                //file without extensions
                previousFolder.add( new FileComponent( extractedKey.getKey().replace( previousFolder.getCompleteName(), " " ).trim(), currentKeyAccessInfo, owner, ownerId, extractedKey.getKey(), extractedKey.getValue() ) );
            }
        }
        return root;
    }

    public FolderComponent convertKeysInFileStructureFormat(Map<String, String> extractedKeys, String bucketName, Map<String, ObjectMetadata> objectMetadata) {
        log.info( "Inside convertKeysInFileStructureFormat" );
        return fileStructureConverter( extractedKeys, bucketName, objectMetadata );
    }

}

