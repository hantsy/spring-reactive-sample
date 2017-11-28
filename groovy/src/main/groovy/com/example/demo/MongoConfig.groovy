package com.example.demo

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableMongoAuditing
@EnableReactiveMongoRepositories(basePackageClasses = [MongoConfig.class])
class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value('${mongo.uri}')
    String mongoUri

    @Override
     MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri)
    }

    @Override
    protected String getDatabaseName() {
        return "blog"
    }

}
