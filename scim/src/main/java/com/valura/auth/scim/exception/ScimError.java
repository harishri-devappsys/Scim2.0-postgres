package com.valura.auth.scim.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimError {
    private String[] schemas = {"urn:ietf:params:scim:api:messages:2.0:Error"};
    private String status;
    private String scimType;
    private String detail;

    public ScimError(String detail, String scimType) {
        this.detail = detail;
        this.scimType = scimType;
    }

    public ScimError(String detail, String scimType, String status) {
        this.detail = detail;
        this.scimType = scimType;
        this.status = status;
    }

    public ScimError(String detail) {
        this.detail = detail;
    }

    public String[] getSchemas() {
        return schemas;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScimType() {
        return scimType;
    }

    public void setScimType(String scimType) {
        this.scimType = scimType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}