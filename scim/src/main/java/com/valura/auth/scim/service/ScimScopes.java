package com.valura.auth.scim.service;

public final class ScimScopes {
    private ScimScopes() {}

    public static final String SCIM_USERS_READ = "scim:users:read";
    public static final String SCIM_USERS_WRITE = "scim:users:write";
    public static final String SCIM_USERS_DELETE = "scim:users:delete";
    public static final String SCIM_GROUPS_READ = "scim:groups:read";
    public static final String SCIM_GROUPS_WRITE = "scim:groups:write";
    public static final String SCIM_GROUPS_DELETE = "scim:groups:delete";
    public static final String SCIM_ADMIN = "scim:admin";
}