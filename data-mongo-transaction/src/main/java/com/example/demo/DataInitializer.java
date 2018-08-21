/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author hantsy
 */
@Component
@Slf4j
class DataInitializer {

    private final ReactiveMongoOperations mongoTemplate;

    public DataInitializer(ReactiveMongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.mongoTemplate.inTransaction()
            .execute(
                s ->
                    Flux
                        .just("Post one", "Post two")
                        .flatMap(
                            title -> s.insert(Post.builder().title(title).content("content of " + title).build())
                        )

            )
            .subscribe(
                v -> log.info("Ok"),
                e -> log.error("error:" + e),
                () -> log.info("done data initialization...")
            );
    }

}