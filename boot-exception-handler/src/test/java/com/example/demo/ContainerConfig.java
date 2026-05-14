package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

	private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:8.0");

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
    	return new MongoDBContainer(MONGO_IMAGE);
    }
}
