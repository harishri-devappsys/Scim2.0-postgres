package com.valura.auth.scim.controller;

import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.scim.model.ScimListResponse;
import com.valura.auth.scim.service.ScimGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scim/v2/Groups")
@ResourceType(description = "Group", name = "Group", schema = GroupResource.class)
public class ScimGroupController {
    private final ScimGroupService groupService;

    public ScimGroupController(ScimGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResource> createGroup(@RequestBody GroupResource group)
            throws ScimException {
        return ResponseEntity.ok(groupService.create(group));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResource> getGroup(@PathVariable String id)
            throws ScimException {
        return ResponseEntity.ok(groupService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResource> updateGroup(
            @PathVariable String id,
            @RequestBody GroupResource group) throws ScimException {
        return ResponseEntity.ok(groupService.replace(id, group));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id)
            throws ScimException {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ScimListResponse<GroupResource>> searchGroups(
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String filter) throws ScimException {
        return ResponseEntity.ok(groupService.search(startIndex, count, filter));
    }
}