package com.valura.auth.scim.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Group;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.common.messages.PatchRequest;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimUser extends UserResource {
    public static class PatchOp {
        private String op;
        private String path;
        private PatchValue value;

        public static class PatchValue {
            private List<Email> emails;
            private List<Group> groups;
            private String displayName;
            private Boolean active;

            // Getters and setters
            public List<Email> getEmails() { return emails; }
            public void setEmails(List<Email> emails) { this.emails = emails; }
            public List<Group> getGroups() { return groups; }
            public void setGroups(List<Group> groups) { this.groups = groups; }
            public String getDisplayName() { return displayName; }
            public void setDisplayName(String displayName) { this.displayName = displayName; }
            public Boolean getActive() { return active; }
            public void setActive(Boolean active) { this.active = active; }
        }

        // Getters and setters
        public String getOp() { return op; }
        public void setOp(String op) { this.op = op; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public PatchValue getValue() { return value; }
        public void setValue(PatchValue value) { this.value = value; }
    }
}