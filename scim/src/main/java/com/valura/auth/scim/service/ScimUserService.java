package com.valura.auth.scim.service;

import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.valura.auth.database.entity.UserEntity;
import com.valura.auth.database.repository.UserRepository;
import com.valura.auth.scim.model.ScimListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ResourceType(description = "User Account", name = "User", schema = UserResource.class)
public class ScimUserService {
    private final UserRepository userRepository;

    public ScimUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResource create(UserResource user) throws BadRequestException {
        if (userRepository.existsByUserName(user.getUserName())) {
            // Use the uniqueness factory method for duplicate username scenarios
            throw BadRequestException.uniqueness("Username already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setExternalId(UUID.randomUUID().toString());
        updateEntityFromScim(entity, user);
        entity = userRepository.save(entity);
        return mapToScimUser(entity);
    }

    public UserResource get(String id) throws ResourceNotFoundException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToScimUser(entity);
    }

    @Transactional
    public UserResource replace(String id, UserResource user) throws ResourceNotFoundException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        updateEntityFromScim(entity, user);
        entity = userRepository.save(entity);
        return mapToScimUser(entity);
    }

    @Transactional
    public void delete(String id) throws ResourceNotFoundException {
        UserEntity entity = userRepository.findByExternalId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(entity);
    }

    public ScimListResponse<UserResource> search(Integer startIndex, Integer count, String filter) {
        Pageable pageable = PageRequest.of(
                startIndex != null ? startIndex - 1 : 0,
                count != null ? count : 100
        );

        Page<UserEntity> page = userRepository.findAll(pageable);
        List<UserResource> resources = page.getContent().stream()
                .map(this::mapToScimUser)
                .collect(Collectors.toList());

        return ScimListResponse.<UserResource>builder()
                .resources(resources)
                .totalResults((int) page.getTotalElements())
                .startIndex(startIndex != null ? startIndex : 1)
                .itemsPerPage(count != null ? count : 100)
                .build();
    }

    private void updateEntityFromScim(UserEntity entity, UserResource user) {
        entity.setUserName(user.getUserName());
        entity.setDisplayName(user.getDisplayName());
        entity.setActive(user.getActive());

        if (user.getEmails() != null && !user.getEmails().isEmpty()) {
            entity.setEmail(user.getEmails().get(0).getValue());
        }
    }

    private UserResource mapToScimUser(UserEntity entity) {
        UserResource scimUser = new UserResource();
        scimUser.setId(entity.getExternalId());
        scimUser.setUserName(entity.getUserName());
        scimUser.setDisplayName(entity.getDisplayName());
        scimUser.setActive(entity.isActive());

        if (entity.getEmail() != null) {
            Email email = new Email();
            email.setValue(entity.getEmail());
            email.setPrimary(true);
            email.setType("work");
            List<Email> emails = new ArrayList<>();
            emails.add(email);
            scimUser.setEmails(emails);
        }

        Meta meta = new Meta();
        meta.setResourceType("User");

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
            meta.setLocation(URI.create("/scim/v2/Users/" + entity.getExternalId()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URI format", e);
        }

        scimUser.setMeta(meta);

        return scimUser;
    }
}