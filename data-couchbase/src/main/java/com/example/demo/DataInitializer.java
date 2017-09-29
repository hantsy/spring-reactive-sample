/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 *
 * @author hantsy
 */
@Component
@Slf4j
class DataInitializer {

    private final PostRepository posts;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.posts
            .deleteAll()
            .thenMany(
                Flux
                    .just("Post one", "Post two")
                    .flatMap(
                        title -> this.posts.save(Post.builder().id(UUID.randomUUID().toString()).title(title).content("content of " + title).build())
                    )
            )
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done initialization...")
            );

    }

}