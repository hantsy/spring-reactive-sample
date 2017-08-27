/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 *
 * @author hantsy
 */
@Component
@Slf4j
class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;
    private final UserRepository users;

    public DataInitializer(PostRepository posts, UserRepository users) {
        this.posts = posts;
        this.users= users;
    }

    @Override
    public void run(String[] args) {
        log.info("start data initialization  ...");
        this.posts
            .deleteAll()
            .thenMany(
                Flux
                    .just("Post one", "Post two")
                    .flatMap((title) -> this.posts.save(Post.builder().title(title).content("content of " + title).build()))
            )
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done posts initialization...")
            );
        
        this.users
            .deleteAll()
            .thenMany(
                Flux
                    .just( 
                        User.builder().username("user").password("password").roles(Arrays.asList("USER")).build(), 
                        User.builder().username("admin").password("password").roles(Arrays.asList("USER, ADMIN")).build()
                    )
                    .flatMap((user) -> this.users.save(user))
            ) 
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done users initialization...")
            );
    }

}
