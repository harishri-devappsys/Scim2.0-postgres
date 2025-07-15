package com.valura.auth.scim.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unboundid.scim2.common.BaseScimResource;
import com.unboundid.scim2.common.types.Meta;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ScimResources extends BaseScimResource {
    private String id;
    private Meta meta;
    private String externalId;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Meta getMeta() {
        return meta;
    }

    @Override
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    @Override
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}