package com.valura.auth.scim.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.valura.auth.scim.exception.ScimAuthorizationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScimAuthorizationService {

    public List<String> extractScopes(DecodedJWT decodedJWT) {
        String scopeClaim = decodedJWT.getClaim("scope").asString();
        if (scopeClaim == null || scopeClaim.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(scopeClaim.split(" "));
    }

    public void checkPermission(String requiredScope) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ScimAuthorizationException("Authentication required.");
        }

        Set<String> userScopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (!userScopes.contains(requiredScope) && !userScopes.contains(ScimScopes.SCIM_ADMIN)) {
            throw new ScimAuthorizationException("Access denied. Missing required scope: " + requiredScope);
        }
    }

    public void checkAnyPermission(String... requiredScopes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ScimAuthorizationException("Authentication required.");
        }

        Set<String> userScopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (userScopes.contains(ScimScopes.SCIM_ADMIN)) {
            return;
        }

        for (String scope : requiredScopes) {
            if (userScopes.contains(scope)) {
                return;
            }
        }
        throw new ScimAuthorizationException("Access denied. Missing one of the required scopes: " + Arrays.toString(requiredScopes));
    }
}