/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author hantsy
 */
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {

    private final ReactiveMongoOperations mongoTemplate;

    private final MongoClient client;


    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");

        var session = client.startSession();
        this.mongoTemplate.withSession(session)
                .execute(
                        s -> Flux.just("Post one", "Post two")
                                .flatMap(
                                        title -> s.insert(Post.builder().title(title).content("content of " + title).build())
                                )
                        ,
                        ClientSession::close
                )
                .subscribe(
                v -> log.info("Ok"),
                e -> log.error("error:" + e),
                () -> log.info("done data initialization...")
        );
    }

}