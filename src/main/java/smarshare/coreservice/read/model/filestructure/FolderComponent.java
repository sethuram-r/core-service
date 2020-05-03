package smarshare.coreservice.read.model.filestructure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

 public class FolderComponent extends BucketComponent {


     @JsonProperty(value = "children")
     List<BucketComponent> bucketComponents = new ArrayList<>(  );

    public FolderComponent(String name, AccessInfo accessInfo, String owner, int ownerId, String completeName) {
        this.name = name;
        this.accessInfo = accessInfo;
        this.owner = owner;
        this.ownerId = ownerId;
        this.completeName = completeName;
    }


    public BucketComponent add (BucketComponent file){
        bucketComponents.add( file );
        return file;
    }

    public BucketComponent remove (BucketComponent file){
        bucketComponents.remove( file );
        return file;
    }

    public List<BucketComponent> getBucketComponents() {
        return bucketComponents;
    }

    //not needed yet
     public BucketComponent getRequiredFolder(String name){
         for (BucketComponent s : this.bucketComponents) {
             if (s.name.equals( name )) {
                 return  s;
             }
         }
         return null;
     }


}
