package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.Duration;

@Slf4j
class MongodbContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        var container = new MongoDBContainer("mongo:8.0")
                .withStartupTimeout(Duration.ofSeconds(60));
        container.start();

        configurableApplicationContext
                .addApplicationListener(e -> {
                            if (e instanceof ContextClosedEvent event) {
                                container.stop();
                            }
                        }
                );
        log.debug("container.getReplicaSetUrl(): {}", container.getReplicaSetUrl("blog"));
        TestPropertyValues
                .of("spring.mongodb.uri=" + container.getReplicaSetUrl("blog"))
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
