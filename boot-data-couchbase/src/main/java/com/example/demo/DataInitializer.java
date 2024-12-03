package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;

    @Override
    public void run(String[] args) {
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
                .log("[initialization log]")
                .subscribe(
                        data -> log.info("saved data: {}", data),
                        error -> log.error("error: {}", error.getMessage()),
                        () -> log.info("done initialization...")
                );

    }

}
