package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
class MongodbContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("mongo");
        var mongoDBContainer = new MongoDBContainer(DEFAULT_IMAGE_NAME).withExposedPorts(27017);
        mongoDBContainer.start();

        configurableApplicationContext
                .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> mongoDBContainer.stop());
        log.debug("mongoDBContainer.getReplicaSetUrl():" + mongoDBContainer.getReplicaSetUrl());
        TestPropertyValues
                .of("spring.data.mongodb.uri=" + mongoDBContainer.getReplicaSetUrl())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
