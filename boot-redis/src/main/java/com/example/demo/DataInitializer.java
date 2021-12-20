package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
@Slf4j
class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String[] args) {
        log.info("start data initialization  ...");
        this.posts.deleteAll()
                .thenMany(
                        Flux.just("Post one", "Post two")
                                .flatMap(title -> {
                                            var id = UUID.randomUUID().toString();
                                            var post = Post.builder().id(id).title(title).content("content of " + title)
                                                    .build();
                                            return this.posts.save(post);
                                        }
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
