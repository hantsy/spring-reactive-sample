package com.example.test

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.core.env.MapPropertySource
import org.testcontainers.mongodb.MongoDBContainer

/**
 * Controls the MongoDB service used by the tests.
 *
 * The container is started before the context is refreshed and stopped when the
 * context is closed, and its URI is published as the `mongo.uri` property so the
 * tests do not rely on an externally started MongoDB instance.
 */
class MongoContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        private val log = LoggerFactory.getLogger(MongoContextInitializer::class.java)
        private const val MONGO_IMAGE = "mongo:8"
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val container = MongoDBContainer(MONGO_IMAGE)
        container.start()
        log.info("MongoDB container started at {}:{}", container.host, container.firstMappedPort)

        applicationContext.addApplicationListener(
            ApplicationListener<ApplicationEvent> { event ->
                if (event is ContextClosedEvent) {
                    container.stop()
                }
            }
        )

        applicationContext.environment
            .propertySources
            .addFirst(
                MapPropertySource(
                    "testproperties",
                    mapOf<String, Any>("mongo.uri" to "mongodb://${container.host}:${container.firstMappedPort}")
                )
            )
    }
}
