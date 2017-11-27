/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

/**
 *
 * @author hantsy
 */
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = arrayOf(MongoConfig::class))
@EnableMongoAuditing
class MongoConfig : AbstractReactiveMongoConfiguration() {

    @Value("\${mongo.uri}")
    lateinit var mongoUri: String

    override fun reactiveMongoClient(): MongoClient = MongoClients.create(mongoUri)

    override fun getDatabaseName(): String = "blog"

}
