package com.valura.auth.scim.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unboundid.scim2.common.types.Meta;
import java.time.Instant;
import java.net.URI;
import java.util.Calendar;
import java.util.TimeZone;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimMeta {
    private String resourceType;
    private Instant created;
    private Instant lastModified;
    private String location;
    private String version;

    public ScimMeta() {
    }

    public ScimMeta(Meta meta) {
        if (meta != null) {
            this.resourceType = meta.getResourceType();
            this.created = meta.getCreated() != null ? meta.getCreated().toInstant() : null;
            this.lastModified = meta.getLastModified() != null ? meta.getLastModified().toInstant() : null;
            this.location = meta.getLocation() != null ? meta.getLocation().toString() : null;
            this.version = meta.getVersion();
        }
    }

    public Meta toUnboundIDMeta() {
        Meta meta = new Meta();
        meta.setResourceType(this.resourceType);

        if (this.created != null) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(this.created.toEpochMilli());
            meta.setCreated(calendar);
        }

        if (this.lastModified != null) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(this.lastModified.toEpochMilli());
            meta.setLastModified(calendar);
        }

        if (this.location != null) {
            meta.setLocation(URI.create(this.location));
        }

        meta.setVersion(this.version);
        return meta;
    }

    // Regular getters and setters
    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    // Builder pattern for convenient creation
    public static class Builder {
        private String resourceType;
        private Instant created;
        private Instant lastModified;
        private String location;
        private String version;

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder created(Instant created) {
            this.created = created;
            return this;
        }

        public Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public ScimMeta build() {
            ScimMeta meta = new ScimMeta();
            meta.resourceType = this.resourceType;
            meta.created = this.created;
            meta.lastModified = this.lastModified;
            meta.location = this.location;
            meta.version = this.version;
            return meta;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}