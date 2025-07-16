package com.valura.auth.scim.exception;

import org.springframework.security.access.AccessDeniedException;

public class ScimAuthorizationException extends AccessDeniedException {
    public ScimAuthorizationException(String msg) {
        super(msg);
    }

    public ScimAuthorizationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}