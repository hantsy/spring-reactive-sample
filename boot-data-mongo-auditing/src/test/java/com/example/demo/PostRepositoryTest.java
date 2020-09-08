package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@Slf4j
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate template;

    @Autowired
    PostRepository posts;

    @BeforeEach
    public void setup() {
        this.template.remove(Post.class).all()
                .subscribe(r -> log.debug("delete all posts: " + r), e -> log.debug("error: " + e), () -> log.debug("done"));
    }

    @Test
    public void testInsertAndQuery() {
        var data = Post.builder().title("testtitle").content("testcontent").build();
        this.template.insert(data)
                .then().block(Duration.ofSeconds(5));

        this.posts.findAll()
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                            log.info("saved post: {}", p);
                            assertEquals("testtitle", p.getTitle());
                            assertNotNull(p.getCreatedAt());
                            assertNotNull(p.getUpdatedAt());
                        }
                )
                .verifyComplete();

    }
}
