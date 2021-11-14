package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Duration;

@Slf4j
class MongodbContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        var mongoDBContainer = new MongoDBContainer("mongo")
                .withStartupTimeout(Duration.ofSeconds(60));
        mongoDBContainer.start();

        configurableApplicationContext
                .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> mongoDBContainer.stop());
        log.debug("mongoDBContainer.getReplicaSetUrl():" + mongoDBContainer.getReplicaSetUrl("blog"));
        TestPropertyValues
                .of("spring.data.mongodb.uri=" + mongoDBContainer.getReplicaSetUrl("blog"))
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
