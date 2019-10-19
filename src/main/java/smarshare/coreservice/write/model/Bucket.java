package smarshare.coreservice.write.model;


public class Bucket {

    private String name;

    public Bucket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "name='" + name + '\'' +
                '}';
    }
}
