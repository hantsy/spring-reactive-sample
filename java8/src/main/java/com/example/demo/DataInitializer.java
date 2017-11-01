/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author hantsy
 */
@Component
@Slf4j
public class DataInitializer {

    private final PostRepository posts;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @PostConstruct
    public void initPosts() {
        log.info("initializing posts data...");
        this.posts.deleteAll();
        Stream.of("Post one", "Post two").forEach(
            title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
        );
    }

}
