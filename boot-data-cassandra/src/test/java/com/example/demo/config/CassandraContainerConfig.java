package com.example.demo.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;

public abstract class CassandraContainerConfig {

    @Container
    static CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra")
            .withInitScript("init.cql")
            .withStartupTimeout(Duration.ofMinutes(5));

    static {
        cassandraContainer.start();
    }

    @DynamicPropertySource
    static void bindCassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.cassandra.keyspace-name", () -> "demo");
        registry.add("spring.data.cassandra.contact-points", () -> "localhost:" + cassandraContainer.getMappedPort(9042));
        registry.add("spring.data.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.data.cassandra.schema-action", () -> "RECREATE");
    }
}
