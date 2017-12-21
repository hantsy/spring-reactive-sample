/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PostRepository posts, UserRepository users, PasswordEncoder passwordEncoder) {
        this.posts = posts;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        initPosts();
        initUsers();
    }

    private void initUsers() {
        log.info("start users initialization  ...");
        this.users
            .deleteAll()
            .thenMany(
                Flux
                    .just("user", "admin")
                    .flatMap(
                        username -> {
                            List<String> roles = "user".equals(username)
                            ? Arrays.asList("ROLE_USER")
                            : Arrays.asList("ROLE_USER", "ROLE_ADMIN");

                            User user = User.builder()
                                    .roles(roles)
                                    .username(username)
                                    .password(passwordEncoder.encode("password"))
                                    .build();
                            return this.users.save(user);
                        }
                    )
            )
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done users initialization...")
            );
    }

    private void initPosts() {
        log.info("start data initialization  ...");
        this.posts
            .deleteAll()
            .thenMany(
                Flux
                    .just("Post one", "Post two")
                    .flatMap(
                        title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
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
