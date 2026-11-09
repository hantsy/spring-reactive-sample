package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringJUnitConfig
@ContextConfiguration(classes = {R2dbcConfig.class, R2dbcPostRepository.class},
        initializers = PostgresContextInitializer.class)
public class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @BeforeEach
    public void setup() {
        this.posts.deleteAll().block();
    }

    @Test
    public void testSaveAll() {
        var data = Post.of("test", "content", Status.PENDING_MODERATION);
        var data1 = Post.of("test1", "content1");

        var result = posts.saveAll(List.of(data, data1))
                .doOnNext(id -> log.debug("generated id: {}", id));

        assertThat(result).isNotNull();
        result.as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void testInsertAndQuery() {
        var data = Post.of("test", "content", Status.PENDING_MODERATION);
        this.posts.save(data)
                .flatMap(id -> this.posts.findById(id))
                .as(StepVerifier::create)
                .consumeNextWith(post -> {
                    log.debug("result data: {}", post);
                    assertThat(post.status()).isEqualTo(Status.PENDING_MODERATION);
                })
                .verifyComplete();
    }
}
