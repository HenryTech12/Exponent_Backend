package com.exponent.demo.exceptionhandler;

import com.exponent.demo.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(Exception ex) {
        String msg = ex.getMessage();
        String lmessgae = ex.getLocalizedMessage();
        String error = ex.toString();

        ErrorResponse errorResponse =
                ErrorResponse.
                        builder()
                        .error(error)
                        .localized_message(lmessgae)
                        .message(msg)
                        .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrityException(DataIntegrityViolationException dataIntegrityViolationException) {

        String msg = dataIntegrityViolationException.getMessage();
        String lmessgae = dataIntegrityViolationException.getLocalizedMessage();
        String error = dataIntegrityViolationException.toString();

        ErrorResponse errorResponse =
                ErrorResponse.
                        builder()
                        .error(error)
                        .localized_message(lmessgae)
                        .message(msg)
                        .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
