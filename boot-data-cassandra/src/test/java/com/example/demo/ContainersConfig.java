package com.example.demo;

import org.testcontainers.cassandra.CassandraContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection(name = "cassandra")
    CassandraContainer cassandraContainer() {
        return new CassandraContainer("cassandra")
            .withInitScript("init.cql")
            .withStartupTimeout(Duration.ofMinutes(5));
    }
}
