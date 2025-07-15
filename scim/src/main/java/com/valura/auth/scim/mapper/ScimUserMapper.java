package com.valura.auth.scim.mapper;

import com.unboundid.scim2.common.types.*;
import com.valura.auth.database.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
public class ScimUserMapper {

    public UserEntity toEntity(UserResource scimUser) {
        UserEntity entity = new UserEntity();
        entity.setUserName(scimUser.getUserName());
        entity.setDisplayName(scimUser.getDisplayName());
        entity.setActive(scimUser.getActive());

        if (scimUser.getEmails() != null && !scimUser.getEmails().isEmpty()) {
            entity.setEmail(scimUser.getEmails().get(0).getValue());
        }

        return entity;
    }

    public UserResource toScim(UserEntity entity) {
        UserResource scimUser = new UserResource();
        scimUser.setId(entity.getExternalId());
        scimUser.setUserName(entity.getUserName());
        scimUser.setDisplayName(entity.getDisplayName());
        scimUser.setActive(entity.isActive());

        // Set email
        if (entity.getEmail() != null) {
            Email email = new Email();
            email.setValue(entity.getEmail());
            email.setPrimary(true);
            email.setType("work");
            List<Email> emails = new ArrayList<>();
            emails.add(email);
            scimUser.setEmails(emails);
        }

        // Set groups
        if (entity.getGroups() != null && !entity.getGroups().isEmpty()) {
            List<Group> groups = entity.getGroups().stream()
                    .map(groupEntity -> {
                        Group group = new Group();
                        group.setValue(groupEntity.getExternalId());
                        group.setDisplay(groupEntity.getDisplayName());
                        try {
                            group.setRef(URI.create("/scim/v2/Groups/" + groupEntity.getExternalId()));
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid URI format", e);
                        }
                        return group;
                    })
                    .collect(Collectors.toList());
            scimUser.setGroups(groups);
        }

        // Set meta information
        Meta meta = new Meta();
        meta.setResourceType("User");

        // Convert Instant to Calendar
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

    private Calendar instantToCalendar(java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(instant.toEpochMilli());
        return calendar;
    }
}