package com.valura.auth.scim.config;

import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.common.utils.SchemaUtils;
import com.unboundid.scim2.server.utils.ResourceTypeDefinition;
import com.unboundid.scim2.common.types.ETagConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.IntrospectionException;

@Configuration
public class ScimConfig {

    @Bean
    public ResourceTypeDefinition userResourceType() throws IntrospectionException {
        return new ResourceTypeDefinition.Builder(
                "User",
                "/scim/v2/Users"
        )
                .setDescription("User Account")
                .setCoreSchema(SchemaUtils.getSchema(UserResource.class))
                .build();
    }

    @Bean
    public ResourceTypeDefinition groupResourceType() throws IntrospectionException {
        return new ResourceTypeDefinition.Builder(
                "Group",
                "/scim/v2/Groups"
        )
                .setDescription("Group")
                .setCoreSchema(SchemaUtils.getSchema(GroupResource.class))
                .build();
    }

    @Bean
    public ETagConfig etagConfig() {
        return new ETagConfig(true);
    }
}