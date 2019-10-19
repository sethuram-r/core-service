package smarshare.coreservice.write.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import smarshare.coreservice.write.exception.BucketExistException;

@RestControllerAdvice
public class BucketExistExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<String> handleBucketExistException(BucketExistException exception) {
        return new ResponseEntity<>( exception.getMessage(), HttpStatus.PRECONDITION_FAILED );
    }
}


