package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = MongoConfig.class)
@TestPropertySource(properties = "mongo.uri=mongodb://localhost:27017/")
@Slf4j
public class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @Test
    public void testGetAllPosts() {
        this.posts.deleteAll()
                .thenMany(
                        Flux.just("Post one", "Post two")
                                .flatMap(
                                        title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
                                )
                )
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertTrue(p.getTitle().contains("one")))
                .consumeNextWith(p -> assertTrue(p.getTitle().contains("two")))
                .expectComplete()
                .verify();
    }

}
