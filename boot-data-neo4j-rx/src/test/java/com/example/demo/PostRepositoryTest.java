package com.example.demo;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.neo4j.springframework.boot.test.autoconfigure.data.DataNeo4jTest;
import org.neo4j.springframework.data.core.ReactiveNeo4jClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataNeo4jTest
class PostRepositoryTest {
    private final static Logger log = LoggerFactory.getLogger(PostRepositoryTest.class);

    @Autowired
    private PostRepository posts;

    @Autowired
    private ReactiveNeo4jClient client;


    @BeforeEach
    void setup() throws IOException {
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
                .then()
                .doOnNext(
                        (v) -> client
                                .query("MATCH (p:Post) RETURN p")
                                .fetchAs(Post.class)
                                .mappedBy((t, r) -> (Post) (r.get("p").asObject()))
                                .all()
                                .subscribe(System.out::println)
                )
                .subscribe(
                        null,
                        null,
                        () -> log.info("done initialization...")
                );
    }

    @AfterEach
    void teardown() {
        this.posts.deleteAll();
    }

    @Test
    void testAllPosts() {
        posts.findAll()
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }

}