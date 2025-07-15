package com.valura.auth.scim.controller;

import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.scim.model.ScimListResponse;
import com.valura.auth.scim.service.ScimUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scim/v2/Users")
@ResourceType(description = "User Account", name = "User", schema = UserResource.class)
public class ScimUserController {
    private final ScimUserService userService;

    public ScimUserController(ScimUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResource> createUser(@RequestBody UserResource user)
            throws ScimException {
        return ResponseEntity.ok(userService.create(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResource> getUser(@PathVariable String id)
            throws ScimException {
        return ResponseEntity.ok(userService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResource> updateUser(
            @PathVariable String id,
            @RequestBody UserResource user) throws ScimException {
        return ResponseEntity.ok(userService.replace(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id)
            throws ScimException {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ScimListResponse<UserResource>> searchUsers(
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String filter) throws ScimException {
        return ResponseEntity.ok(userService.search(startIndex, count, filter));
    }
}