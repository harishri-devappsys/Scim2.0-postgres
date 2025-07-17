package com.valura.auth.scim.controller;

import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.exceptions.ResourceConflictException;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.scim.model.ScimListResponse;
import com.valura.auth.scim.service.ScimAuthorizationService;
import com.valura.auth.scim.service.ScimScopes;
import com.valura.auth.scim.service.ScimUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/scim/v2/Users")
@ResourceType(description = "User Account", name = "User", schema = UserResource.class)
public class ScimUserController {
    private final ScimUserService userService;

    @Autowired
    private ScimAuthorizationService scimAuthorizationService;

    public ScimUserController(ScimUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResource> createUser(@RequestBody UserResource user)
            throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_USERS_WRITE);
        UserResource createdUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .eTag(createdUser.getMeta().getVersion())
                .body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResource> getUser(@PathVariable String id)
            throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_USERS_READ);
        UserResource user = userService.get(id);
        return ResponseEntity.ok()
                .eTag(user.getMeta().getVersion())
                .body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResource> updateUser(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody UserResource user) throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_USERS_WRITE);
        UserResource updatedUser = userService.replace(id, user, ifMatch);
        return ResponseEntity.ok()
                .eTag(updatedUser.getMeta().getVersion())
                .body(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResource> patchUser(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody UserResource user) throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_USERS_WRITE);
        UserResource patchedUser = userService.patch(id, user, ifMatch);
        return ResponseEntity.ok()
                .eTag(patchedUser.getMeta().getVersion())
                .body(patchedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id)
            throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_USERS_DELETE);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ScimListResponse<UserResource>> searchUsers(
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String filter) throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_USERS_READ);
        ScimListResponse<UserResource> response = userService.search(startIndex, count, filter);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<String> handleResourceConflictException(ResourceConflictException ex) {
        String errorBody = "{"
                + "\"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:Error\"],"
                + "\"status\": \"409\","
                + "\"scimType\": \"uniqueness\","
                + "\"detail\": \"" + ex.getMessage() + "\""
                + "}";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(HttpHeaders.CONTENT_TYPE, "application/scim+json;charset=utf-8")
                .body(errorBody);
    }
}