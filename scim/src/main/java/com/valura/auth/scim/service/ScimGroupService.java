package com.valura.auth.scim.service;

import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.database.entity.GroupEntity;
import com.valura.auth.database.repository.GroupRepository;
import com.valura.auth.scim.model.ScimListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ResourceType(description = "Group", name = "Group", schema = GroupResource.class)
public class ScimGroupService {
    private final GroupRepository groupRepository;

    public ScimGroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional
    public GroupResource create(GroupResource group) {
        GroupEntity entity = new GroupEntity();
        entity.setExternalId(UUID.randomUUID().toString());
        entity.setDisplayName(group.getDisplayName());
        entity = groupRepository.save(entity);
        return mapToScimGroup(entity);
    }

    public GroupResource get(String id) throws ResourceNotFoundException {
        GroupEntity entity = groupRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return mapToScimGroup(entity);
    }

    @Transactional
    public GroupResource replace(String id, GroupResource group) throws ResourceNotFoundException {
        GroupEntity entity = groupRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        entity.setDisplayName(group.getDisplayName());
        entity = groupRepository.save(entity);
        return mapToScimGroup(entity);
    }

    @Transactional
    public void delete(String id) throws ResourceNotFoundException {
        GroupEntity entity = groupRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        groupRepository.delete(entity);
    }

    public ScimListResponse<GroupResource> search(Integer startIndex, Integer count, String filter) {
        Pageable pageable = PageRequest.of(
                startIndex != null ? startIndex - 1 : 0,
                count != null ? count : 100
        );

        Page<GroupEntity> page = groupRepository.findAll(pageable);
        List<GroupResource> resources = page.getContent().stream()
                .map(this::mapToScimGroup)
                .collect(Collectors.toList());

        return ScimListResponse.<GroupResource>builder()
                .resources(resources)
                .totalResults((int) page.getTotalElements())
                .startIndex(startIndex != null ? startIndex : 1)
                .itemsPerPage(count != null ? count : 100)
                .build();
    }

    private GroupResource mapToScimGroup(GroupEntity entity) {
        GroupResource scimGroup = new GroupResource();
        scimGroup.setId(entity.getExternalId());
        scimGroup.setDisplayName(entity.getDisplayName());

        Meta meta = new Meta();
        meta.setResourceType("Group");

        // Set created and last modified dates
        if (entity.getCreatedAt() != null) {
            Calendar created = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            created.setTimeInMillis(entity.getCreatedAt().toEpochMilli());
            meta.setCreated(created);
        }

        if (entity.getUpdatedAt() != null) {
            Calendar modified = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            modified.setTimeInMillis(entity.getUpdatedAt().toEpochMilli());
            meta.setLastModified(modified);
        }

        try {
            meta.setLocation(URI.create("/scim/v2/Groups/" + entity.getExternalId()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URI format", e);
        }

        scimGroup.setMeta(meta);

        return scimGroup;
    }
}