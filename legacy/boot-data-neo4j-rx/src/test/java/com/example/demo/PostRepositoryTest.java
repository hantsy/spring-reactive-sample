package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration;
import org.neo4j.springframework.boot.test.autoconfigure.data.ReactiveDataNeo4jTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@ReactiveDataNeo4jTest(excludeAutoConfiguration = Neo4jTestHarnessAutoConfiguration.class)
@Slf4j
public class PostRepositoryTest {

//    @Container
//    private static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.0");

    /**
     * Note: This code fragment is from Neo4j Data Rx spring boot test starter.
     *
     * An initializer that starts a Neo4j test container and sets {@code org.neo4j.driver.uri} to the containers
     * bolt uri. It also registers an application listener that stops the container when the context closes.
     */
    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.0").withoutAuthentication();
            neo4jContainer.start();
            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> neo4jContainer.stop());
            TestPropertyValues.of("org.neo4j.driver.uri=" + neo4jContainer.getBoltUrl())
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }


    @Autowired
    private PostRepository posts;

    @BeforeEach
    public void setup() throws IOException {
        log.debug("running setup.....,");
        this.posts.deleteAll()
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
                .flatMap(it -> this.posts.save(it));
    }

    private Flux<Post> testFoundMethod() {
        return this.posts
                .findAll(Example.of(Post.builder().title("Post one").build()));
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