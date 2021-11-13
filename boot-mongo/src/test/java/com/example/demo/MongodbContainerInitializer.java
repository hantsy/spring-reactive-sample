package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.MongoDBContainer;

@Slf4j
class MongodbContainerInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    var container = new MongoDBContainer("mongo:4");
    container.start();

    configurableApplicationContext
        .addApplicationListener(
            (ApplicationListener<ContextClosedEvent>) event -> container.stop());
    log.debug("container.getFirstMappedPort():" + container.getFirstMappedPort());
    TestPropertyValues
        .of("spring.data.mongodb.uri=mongodb://localhost:" + container.getFirstMappedPort()
            + "/blog")
        .applyTo(configurableApplicationContext.getEnvironment());
  }
}
