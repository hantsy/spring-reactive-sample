package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.MongoDBContainer;

@Slf4j
class MongodbContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        var mongoDBContainer = new MongoDBContainer();
        mongoDBContainer.start();

        configurableApplicationContext
                .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> mongoDBContainer.stop());
        log.debug("mongoDBContainer.getReplicaSetUrl():" + mongoDBContainer.getReplicaSetUrl());
        TestPropertyValues
                .of("spring.data.mongodb.uri=" + mongoDBContainer.getReplicaSetUrl())
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
