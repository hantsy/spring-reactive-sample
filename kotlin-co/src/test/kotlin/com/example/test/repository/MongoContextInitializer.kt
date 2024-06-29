package com.example.test.repository

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.core.env.MapPropertySource
import org.testcontainers.containers.MongoDBContainer
import java.util.Map


class MongoContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        private val log = LoggerFactory.getLogger(MongoContextInitializer::class.java)
        private val container = MongoDBContainer("mongo")
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        container.start()
        log.info(" container.getFirstMappedPort():: {}", container.firstMappedPort)
        applicationContext
            .addApplicationListener(ApplicationListener { _: ContextClosedEvent -> container.stop() })

        applicationContext.environment
            .propertySources
            .addFirst(
                MapPropertySource(
                    "testproperties",
                    Map.of<String, Any>("mongo.uri", "mongodb://${container.host}:${container.firstMappedPort}")
                )
            )

    }
}