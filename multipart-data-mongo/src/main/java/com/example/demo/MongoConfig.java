package com.example.demo;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * @author hantsy
 */
@Configuration
@EnableReactiveMongoRepositories
public class MongoConfig extends AbstractReactiveMongoConfiguration {

  @Value("${mongo.uri}")
  String mongoUri;

  @Override
  protected String getDatabaseName() {
    return "blog";
  }


  @Override
  public MongoClient reactiveMongoClient() {
    return MongoClients.create(mongoUri);
  }

  @Bean
  public ReactiveGridFsTemplate reactiveGridFsTemplate(
      ReactiveMongoDatabaseFactory databaseFactory,
      MappingMongoConverter mongoConverter) {
    return new ReactiveGridFsTemplate(databaseFactory, mongoConverter);
  }


}
