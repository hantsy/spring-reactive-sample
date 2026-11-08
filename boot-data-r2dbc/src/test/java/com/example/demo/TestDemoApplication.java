package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestDemoApplication {
    
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:14"));
    }
    
    public static void main(String[] args) {
        SpringApplication.from(DemoApplication::main)
                .with(TestDemoApplication.class)
                .run(args);
    }
}
