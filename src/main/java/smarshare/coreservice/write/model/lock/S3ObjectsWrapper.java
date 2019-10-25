package smarshare.coreservice.write.model.lock;

import java.util.Iterator;
import java.util.List;

public class S3ObjectsWrapper implements Iterable {

    private List<S3Object> objects;

    public S3ObjectsWrapper(List<S3Object> objects) {
        this.objects = objects;
    }

    @Override
    public Iterator<S3Object> iterator() {
        return this.objects.iterator();
    }
}
