package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    private static final String DEFAULT_IMAGE_NAME = "couchbase/server";

    @Bean
    @ServiceConnection
    public CouchbaseContainer couchbaseContainer() {
        return new CouchbaseContainer(DEFAULT_IMAGE_NAME)
                .withCredentials("Administrator", "password")
                .withBucket(new BucketDefinition("demo").withPrimaryIndex(true))
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofSeconds(120));
    }
}
