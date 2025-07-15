package com.valura.auth.database.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.valura.auth.database.entity")
@EnableJpaRepositories(basePackages = "com.valura.auth.database.repository")
public class DatabaseConfig {
    // Configuration can be extended based on needs
}