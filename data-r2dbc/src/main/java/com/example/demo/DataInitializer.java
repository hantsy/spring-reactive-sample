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
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.data.domain.Sort.Order.desc;

/**
 * @author hantsy
 */
@Component
@Slf4j
class DataInitializer {

    private final DatabaseClient databaseClient;

    public DataInitializer(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.databaseClient.insert()
            .into("posts")
            //.nullValue("id", Integer.class)
            .value("title", "First post title")
            .value("content", "Content of my first post")
            .exchange() //
            .flatMapMany(it -> it.extract((r, m) -> r.get("id", Integer.class)).all())
            .log()
            .thenMany(
                this.databaseClient.select()
                    .from("posts")
                    .orderBy(Sort.by(desc("id")))
                    .as(Post.class)
                    .fetch()
                    .all()
                    .log()
            )
            .subscribe(null, null, () -> log.info("initialization is done..."));
    }

}