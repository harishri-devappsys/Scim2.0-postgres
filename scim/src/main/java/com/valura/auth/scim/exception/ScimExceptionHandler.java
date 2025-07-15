package com.valura.auth.scim.exception;

import com.unboundid.scim2.common.exceptions.ScimException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ScimExceptionHandler {

    @ExceptionHandler(ScimException.class)
    public ResponseEntity<ScimError> handleScimException(ScimException ex) {
        com.valura.auth.scim.exception.ScimException convertedException =
                new com.valura.auth.scim.exception.ScimException(ex);

        ScimError error = new ScimError(ex.getMessage(), convertedException.getScimType());
        error.setStatus(String.valueOf(convertedException.getStatus().value()));

        return new ResponseEntity<>(error, convertedException.getStatus());
    }

    @ExceptionHandler(com.valura.auth.scim.exception.ScimException.class)
    public ResponseEntity<ScimError> handleCustomScimException(
            com.valura.auth.scim.exception.ScimException ex) {
        ScimError error = new ScimError(ex.getMessage(), ex.getScimType());
        error.setStatus(String.valueOf(ex.getStatus().value()));

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ScimError> handleGenericException(Exception ex) {
        ScimError error = new ScimError(
                "An unexpected error occurred",
                "serverError"
        );
        error.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}