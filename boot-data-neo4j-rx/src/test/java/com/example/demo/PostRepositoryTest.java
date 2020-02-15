package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.springframework.boot.test.autoconfigure.data.DataNeo4jTest;
import org.neo4j.springframework.data.core.ReactiveNeo4jOperations;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.neo4j.springframework.data.core.cypher.Cypher.*;

@DataNeo4jTest
@Slf4j
public class PostRepositoryTest {

    @Autowired
    private PostRepository posts;

    @Autowired
    private ReactiveNeo4jOperations operations;


    @BeforeEach
    public void setup() throws IOException {
        log.debug("running setup.....,");
        this.operations.deleteAll(Post.class)
                .thenMany(testSaveMethod())
                .log()
                .thenMany(testFoundMethod())
                .log()
                .subscribe(
                        (data) -> log.info("found post:" + data),
                        (err) -> log.error("" + err),
                        () -> log.info("done")
                );
    }

    private Flux<Post> testSaveMethod() {
        var data = Stream.of("Post one", "Post two")
                .map(title -> Post.builder().title(title).content("The content of " + title).build())
                .collect(Collectors.toList());
        return Flux.fromIterable(data)
                .flatMap(it -> this.operations.save(it));
    }

    private Flux<Post> testFoundMethod() {
        var postNode = node("Post").named("p");
        return this.operations
                .findAll(
                        match(postNode)
                                .where(postNode.property("title").contains(literalOf("one")))
                                .returning(postNode)
                                .build(),
                        Post.class
                );
    }

    @AfterEach
    void teardown() {
        //this.posts.deleteAll();
    }

    @Test
    void testAllPosts() {
        posts.findAll().sort(Comparator.comparing(post -> post.getTitle()))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }

}