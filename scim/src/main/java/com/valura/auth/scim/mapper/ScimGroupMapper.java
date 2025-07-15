package com.valura.auth.scim.mapper;

import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Meta;
import com.valura.auth.database.entity.GroupEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@Component
public class ScimGroupMapper {
    public GroupEntity toEntity(GroupResource scimGroup) {
        GroupEntity entity = new GroupEntity();
        entity.setExternalId(scimGroup.getId() != null ? scimGroup.getId() : UUID.randomUUID().toString());
        entity.setDisplayName(scimGroup.getDisplayName());
        return entity;
    }

    public GroupResource toScim(GroupEntity entity) {
        GroupResource scimGroup = new GroupResource();
        scimGroup.setId(entity.getExternalId());
        scimGroup.setDisplayName(entity.getDisplayName());

        // Set meta information
        Meta meta = new Meta();
        meta.setResourceType("Group");

        // Convert Instant to Calendar
        meta.setCreated(instantToCalendar(entity.getCreatedAt()));
        meta.setLastModified(instantToCalendar(entity.getUpdatedAt()));

        // Convert String to URI
        try {
            meta.setLocation(URI.create("/scim/v2/Groups/" + entity.getExternalId()));
        } catch (IllegalArgumentException e) {
            // Handle invalid URI format
            throw new RuntimeException("Invalid URI format", e);
        }

        scimGroup.setMeta(meta);

        return scimGroup;
    }

    private Calendar instantToCalendar(Instant instant) {
        if (instant == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(instant.toEpochMilli());
        return calendar;
    }
}