package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration;
import org.neo4j.springframework.boot.test.autoconfigure.data.ReactiveDataNeo4jTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ReactiveDataNeo4jTest(excludeAutoConfiguration = Neo4jTestHarnessAutoConfiguration.class)
@Slf4j
class PostRepositoryTest extends Neo4jContainerSetUp{
    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    public void setup() throws IOException {
        log.debug("running setup.....,");
        this.postRepository.deleteAll()
                .thenMany(testSaveMethod())
                .log()
                .thenMany(testFoundMethod())
                .log()
                .blockLast();// to make the tests work
//                .subscribe(
//                        (data) -> log.info("found post:" + data),
//                        (err) -> log.error("" + err),
//                        () -> log.info("done")
//                );
    }

    private Flux<Post> testSaveMethod() {
        var data = Stream.of("Post one", "Post two")
                .map(title -> Post.builder().title(title).content("The content of " + title).build())
                .collect(Collectors.toList());
        return Flux.fromIterable(data)
                .flatMap(it -> this.postRepository.save(it));
    }

    private Flux<Post> testFoundMethod() {
        return this.postRepository
                .findAll(Example.of(Post.builder().title("Post one").build()));
    }

    @AfterEach
    void teardown() {
        //this.posts.deleteAll();
    }

    @Test
    void testAllPosts() {
        postRepository.findAll().sort(Comparator.comparing(post -> post.getTitle()))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }


}