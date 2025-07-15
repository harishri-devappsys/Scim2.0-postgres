package com.valura.auth.scim.service;

import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.Member;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.database.entity.GroupEntity;
import com.valura.auth.database.entity.UserEntity;
import com.valura.auth.database.repository.GroupRepository;
import com.valura.auth.database.repository.UserRepository;
import com.valura.auth.scim.model.ScimListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ResourceType(description = "Group", name = "Group", schema = GroupResource.class)
public class ScimGroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public ScimGroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GroupResource create(GroupResource group) throws ResourceNotFoundException {
        GroupEntity entity = new GroupEntity();
        entity.setExternalId(UUID.randomUUID().toString());
        entity.setDisplayName(group.getDisplayName());

        // Save the group first
        entity = groupRepository.save(entity);

        // Handle members - use the helper method to maintain bidirectional relationship
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            for (Member member : group.getMembers()) {
                UserEntity user = userRepository.findByExternalId(member.getValue())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + member.getValue()));
                entity.addMember(user);
            }
            // Save the users to persist the relationship (since UserEntity is the owning side)
            entity.getMembers().forEach(userRepository::save);
        }

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

        // Clear existing members first
        Set<UserEntity> existingMembers = new HashSet<>(entity.getMembers());
        for (UserEntity user : existingMembers) {
            entity.removeMember(user);
        }

        // Save users to persist the removal
        existingMembers.forEach(userRepository::save);

        // Handle new members
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            for (Member member : group.getMembers()) {
                UserEntity user = userRepository.findByExternalId(member.getValue())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + member.getValue()));
                entity.addMember(user);
            }
            // Save users to persist the new relationships
            entity.getMembers().forEach(userRepository::save);
        }

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

        // Map members
        if (entity.getMembers() != null && !entity.getMembers().isEmpty()) {
            List<Member> members = entity.getMembers().stream()
                    .map(userEntity -> {
                        Member member = new Member();
                        member.setValue(userEntity.getExternalId());
                        member.setRef(URI.create("/scim/v2/Users/" + userEntity.getExternalId()));
                        member.setDisplay(userEntity.getUserName()); // or getDisplayName() if available
                        return member;
                    })
                    .collect(Collectors.toList());
            scimGroup.setMembers(members);
        }

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