package com.example.demo

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.lang.Nullable

@Configuration
@EnableMongoAuditing
@EnableReactiveMongoRepositories(basePackageClasses = [MongoConfig.class])
class MongoConfig extends AbstractReactiveMongoConfiguration {
//
//    @Value('${mongo.uri}')
//    String mongoUri

    @Override
    MongoClient reactiveMongoClient() {
        return MongoClients.create("mongodb://localhost:27017/blog")
    }

    @Override
    protected String getDatabaseName() {
        return "blog"
    }

}
