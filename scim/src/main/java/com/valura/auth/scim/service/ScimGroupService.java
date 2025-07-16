package com.valura.auth.scim.service;

import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.exceptions.PreconditionFailedException;
import com.unboundid.scim2.common.exceptions.ScimException;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;

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

        entity = groupRepository.save(entity);

        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            for (Member member : group.getMembers()) {
                UserEntity user = userRepository.findByExternalId(member.getValue())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + member.getValue()));
                entity.addMember(user);
            }
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
    public GroupResource replace(String id, GroupResource group, String ifMatch) throws ScimException {
        GroupEntity entity = groupRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        GroupResource existingGroup = mapToScimGroup(entity);
        validateETag(existingGroup.getMeta().getVersion(), ifMatch);

        entity.setDisplayName(group.getDisplayName());

        Set<UserEntity> existingMembers = new HashSet<>(entity.getMembers());
        for (UserEntity user : existingMembers) {
            entity.removeMember(user);
        }
        existingMembers.forEach(userRepository::save);

        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            for (Member member : group.getMembers()) {
                UserEntity user = userRepository.findByExternalId(member.getValue())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + member.getValue()));
                entity.addMember(user);
            }
            entity.getMembers().forEach(userRepository::save);
        }

        entity = groupRepository.save(entity);
        return mapToScimGroup(entity);
    }

    @Transactional
    public GroupResource patch(String id, GroupResource group, String ifMatch) throws ScimException {
        GroupEntity entity = groupRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        GroupResource existingGroup = mapToScimGroup(entity);
        validateETag(existingGroup.getMeta().getVersion(), ifMatch);

        if (group.getDisplayName() != null) {
            entity.setDisplayName(group.getDisplayName());
        }

        // Simplified member handling for PATCH. A full PATCH implementation would involve
        // parsing the "add", "remove", "replace" operations for members.
        // For now, if members are provided, it replaces the entire set.
        if (group.getMembers() != null) {
            Set<UserEntity> existingMembers = new HashSet<>(entity.getMembers());
            for (UserEntity user : existingMembers) {
                entity.removeMember(user);
            }
            existingMembers.forEach(userRepository::save);

            for (Member member : group.getMembers()) {
                UserEntity user = userRepository.findByExternalId(member.getValue())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + member.getValue()));
                entity.addMember(user);
            }
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

        if (entity.getMembers() != null && !entity.getMembers().isEmpty()) {
            List<Member> members = entity.getMembers().stream()
                    .map(userEntity -> {
                        Member member = new Member();
                        member.setValue(userEntity.getExternalId());
                        member.setRef(URI.create("/scim/v2/Users/" + userEntity.getExternalId()));
                        member.setDisplay(userEntity.getUserName());
                        return member;
                    })
                    .collect(Collectors.toList());
            scimGroup.setMembers(members);
        }

        Meta meta = new Meta();
        meta.setResourceType("Group");

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
            meta.setVersion(generateETag(scimGroup));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URI format", e);
        }

        scimGroup.setMeta(meta);

        return scimGroup;
    }

    private String generateETag(GroupResource groupResource) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String data = groupResource.getId() + groupResource.getDisplayName() +
                    (groupResource.getMeta() != null && groupResource.getMeta().getLastModified() != null ?
                            groupResource.getMeta().getLastModified().getTimeInMillis() : "");
            // Include members in ETag calculation if they significantly affect the group state
            if (groupResource.getMembers() != null) {
                for (Member member : groupResource.getMembers()) {
                    data += member.getValue();
                }
            }
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return "W/\"" + DatatypeConverter.printHexBinary(hash).toLowerCase() + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found for ETag generation", e);
        }
    }

    private void validateETag(String currentETag, String ifMatchHeader) throws PreconditionFailedException {
        if (ifMatchHeader != null && !ifMatchHeader.isEmpty()) {
            if (!currentETag.equals(ifMatchHeader)) {
                throw new PreconditionFailedException("ETag mismatch");
            }
        }
    }
}