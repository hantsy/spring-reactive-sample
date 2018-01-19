/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 *
 * @author hantsy
 */
@Component
@Slf4j
public class DataInitializer {

    private final RxJava2PostRepository posts;

    public DataInitializer(RxJava2PostRepository posts) {
        this.posts = posts;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initPosts() {
        log.info("initializing posts data...");
        Stream.of("Post one", "Post two").forEach(
            title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
                .subscribe()
        );
    }

}
