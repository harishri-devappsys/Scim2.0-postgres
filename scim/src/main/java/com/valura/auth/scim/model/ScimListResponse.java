package com.valura.auth.scim.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimListResponse<T> {

    @JsonProperty("Resources")
    private List<T> resources;
    private int totalResults;
    private Integer startIndex;
    private Integer itemsPerPage;
    private final String[] schemas = {"urn:ietf:params:scim:api:messages:2.0:ListResponse"};

    public ScimListResponse() {
    }

    public ScimListResponse(List<T> resources, int totalResults, Integer startIndex, Integer itemsPerPage) {
        this.resources = resources;
        this.totalResults = totalResults;
        this.startIndex = startIndex;
        this.itemsPerPage = itemsPerPage;
    }

    public List<T> getResources() {
        return resources;
    }

    public void setResources(List<T> resources) {
        this.resources = resources;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public String[] getSchemas() {
        return schemas;
    }

    // Builder pattern for convenient creation
    public static class Builder<T> {
        private List<T> resources;
        private int totalResults;
        private Integer startIndex;
        private Integer itemsPerPage;

        public Builder<T> resources(List<T> resources) {
            this.resources = resources;
            return this;
        }

        public Builder<T> totalResults(int totalResults) {
            this.totalResults = totalResults;
            return this;
        }

        public Builder<T> startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder<T> itemsPerPage(Integer itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public ScimListResponse<T> build() {
            return new ScimListResponse<>(resources, totalResults, startIndex, itemsPerPage);
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}