/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static org.springframework.data.domain.Sort.Order.desc;

/** @author hantsy */
@Component
@Slf4j
class DataInitializer {

  private final DatabaseClient databaseClient;

  public DataInitializer(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @EventListener(value = ContextRefreshedEvent.class)
  public void init() {
    log.info("start data initialization...");
    this.databaseClient
        .sql(() -> "delete from posts")
        .then()
        .and(
            this.databaseClient
                .sql("INSERT INTO  posts (title, content) VALUES (:title, :content)")
                .filter(
                    (statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .bind("title", "First post title")
                .bind("content", "Content of my first post")
                .map((row, rowMetadata) -> row.get(0, Integer.class))
                .all()
                .log())
        .thenMany(this.databaseClient.sql(" select * from posts").fetch().all().log())
        .subscribe(null, null, () -> log.info("initialization is done..."));
  }
}
