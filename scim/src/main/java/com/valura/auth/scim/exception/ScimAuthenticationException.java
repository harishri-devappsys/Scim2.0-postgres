package com.valura.auth.scim.exception;

import org.springframework.security.core.AuthenticationException;

public class ScimAuthenticationException extends AuthenticationException {
    public ScimAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ScimAuthenticationException(String msg) {
        super(msg);
    }
}