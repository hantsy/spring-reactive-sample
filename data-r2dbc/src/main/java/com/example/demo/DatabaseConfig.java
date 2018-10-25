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

/**
 *
 * @author hantsy
 */
@Configuration
public class DatabaseConfig {

    @Bean
    DatabaseClient databaseClient(){
        PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
            .host("localhost")
            .database("test")
            .username("user")
            .password("password").build()
        );

        DatabaseClient databaseClient = DatabaseClient.create(connectionFactory);

        return databaseClient;
    }

}

