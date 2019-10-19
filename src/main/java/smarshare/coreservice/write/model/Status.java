package smarshare.coreservice.write.model;

public class Status {

    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Status{" +
                "message='" + message + '\'' +
                '}';
    }
}
