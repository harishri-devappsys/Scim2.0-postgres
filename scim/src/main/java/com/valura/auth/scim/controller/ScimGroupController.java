package com.valura.auth.scim.controller;

import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.scim.model.ScimListResponse;
import com.valura.auth.scim.service.ScimAuthorizationService;
import com.valura.auth.scim.service.ScimGroupService;
import com.valura.auth.scim.service.ScimScopes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/scim/v2/Groups")
@ResourceType(description = "Group", name = "Group", schema = GroupResource.class)
public class ScimGroupController {
    private final ScimGroupService groupService;

    @Autowired
    private ScimAuthorizationService scimAuthorizationService;

    public ScimGroupController(ScimGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResource> createGroup(@RequestBody GroupResource group)
            throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_GROUPS_WRITE);
        GroupResource createdGroup = groupService.create(group);
        return ResponseEntity.status(HttpStatus.CREATED)
                .eTag(createdGroup.getMeta().getVersion())
                .body(createdGroup);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResource> getGroup(@PathVariable String id)
            throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_GROUPS_READ);
        GroupResource group = groupService.get(id);
        return ResponseEntity.ok()
                .eTag(group.getMeta().getVersion())
                .body(group);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResource> updateGroup(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody GroupResource group) throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_GROUPS_WRITE);
        GroupResource updatedGroup = groupService.replace(id, group, ifMatch);
        return ResponseEntity.ok()
                .eTag(updatedGroup.getMeta().getVersion())
                .body(updatedGroup);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GroupResource> patchGroup(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody GroupResource group) throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_GROUPS_WRITE);
        GroupResource patchedGroup = groupService.patch(id, group, ifMatch); // Assuming a patch method in service
        return ResponseEntity.ok()
                .eTag(patchedGroup.getMeta().getVersion())
                .body(patchedGroup);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id)
            throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_GROUPS_DELETE);
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ScimListResponse<GroupResource>> searchGroups(
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String filter) throws ScimException {
        scimAuthorizationService.checkPermission(ScimScopes.SCIM_GROUPS_READ);
        ScimListResponse<GroupResource> response = groupService.search(startIndex, count, filter);
        return ResponseEntity.ok(response);
    }
}