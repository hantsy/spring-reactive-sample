package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    private static final String COUCHBASE_IMAGE_NAME = "couchbase";
    private static final String DEFAULT_IMAGE_NAME = "couchbase/server:7";
    private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse(COUCHBASE_IMAGE_NAME)
            .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

    @Bean
    @ServiceConnection
    public CouchbaseContainer couchbaseContainer() {
        return new CouchbaseContainer(DEFAULT_IMAGE)
                .withCredentials("Administrator", "password")
                .withBucket(new BucketDefinition("demo").withPrimaryIndex(true))
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofSeconds(120))
                .waitingFor(Wait.forHealthcheck());
    }
}
