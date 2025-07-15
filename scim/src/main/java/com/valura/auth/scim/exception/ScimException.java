package com.valura.auth.scim.exception;

import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

public class ScimException extends RuntimeException {
    private final HttpStatus status;
    private final String scimType;

    public ScimException(String message, HttpStatus status, String scimType) {
        super(message);
        this.status = status;
        this.scimType = scimType;
    }

    public ScimException(com.unboundid.scim2.common.exceptions.ScimException ex) {
        super(ex.getMessage());
        if (ex instanceof ResourceNotFoundException) {
            this.status = HttpStatus.NOT_FOUND;
            this.scimType = "invalidResource";
        } else if (ex instanceof BadRequestException) {
            this.status = HttpStatus.BAD_REQUEST;
            this.scimType = "invalidSyntax";
        } else {
            this.status = HttpStatus.INTERNAL_SERVER_ERROR;
            this.scimType = "serverError";
        }
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getScimType() {
        return scimType;
    }
}