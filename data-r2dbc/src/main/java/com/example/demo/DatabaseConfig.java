/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

/**
 * @author hantsy
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public PostgresqlConnectionFactory postgresqlConnectionFactory() {
        return new PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host("localhost")
                .database("test")
                .username("user")
                .password("password").build()
        );
    }

    @Bean
    DatabaseClient databaseClient(PostgresqlConnectionFactory connectionFactory ) {
        return DatabaseClient.create(connectionFactory);
    }

    @Bean
    PostRepository repository(R2dbcRepositoryFactory factory) {
        return factory.getRepository(PostRepository.class);
    }

    @Bean
    R2dbcRepositoryFactory factory(DatabaseClient client) {
        RelationalMappingContext context = new RelationalMappingContext();
        context.afterPropertiesSet();
        return new R2dbcRepositoryFactory(client, context);
    }

}

