/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import static java.util.Collections.singletonList;

/**
 * @author hantsy
 */
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = {MongoConfig.class})
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Autowired
    Environment environment;

    @Override
    protected String getDatabaseName() {
        return "blog";
    }

    @Bean
    @Override
    public MongoClient reactiveMongoClient() {
        return super.reactiveMongoClient();
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {

        builder
                //.credential(MongoCredential.createCredential("name", "db", "pwd".toCharArray()))
                .applyToClusterSettings(settings  -> {
                    settings.hosts(singletonList(new ServerAddress(environment.getProperty("mongo.host"), environment.getProperty("mongo.port", Integer.class))));
                });
    }

    @Bean
    ReactiveMongoTransactionManager reactiveMongoTransactionManager(ReactiveMongoDatabaseFactory databaseFactory) {
        return new ReactiveMongoTransactionManager(databaseFactory);
    }

}
