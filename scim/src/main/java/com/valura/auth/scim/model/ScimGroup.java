package com.valura.auth.scim.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.messages.PatchRequest;
import com.unboundid.scim2.common.types.Member;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScimGroup extends GroupResource {
    public static class PatchOp {
        private String op;
        private String path;
        private PatchValue value;

        public static class PatchValue {
            private List<Member> members;

            public List<Member> getMembers() { return members; }
            public void setMembers(List<Member> members) { this.members = members; }
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