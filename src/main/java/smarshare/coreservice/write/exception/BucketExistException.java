package smarshare.coreservice.write.exception;


public class BucketExistException extends RuntimeException {

    private String message;

    public BucketExistException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}


