package com.example.demo

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@EnableReactiveMongoRepositories
@EnableMongoAuditing
class DataConfig(val environment: Environment) : AbstractReactiveMongoConfiguration() {

    @Bean
    fun mongoEventListener(): LoggingEventListener {
        return LoggingEventListener()
    }

    @Bean
    override fun mongoClient() = MongoClients.create(ConnectionString(environment.getProperty("mongo.uri")))

    @Bean
    override fun getDatabaseName(): String = "blog"
}