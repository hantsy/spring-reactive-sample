package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Slf4j
public class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.posts.deleteAll()
                .doOnTerminate(latch::countDown)
                .subscribe(del -> log.debug("deleted posts: {}", del));
        latch.await(5000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSaveAll() {
        var data = Post.of("test", "content", Status.PENDING_MODERATION);
        var data1 = Post.of("test1", "content1");

        var result = posts.saveAll(List.of(data, data1)).log("[Generated result]")
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
                .consumeNextWith(r -> {
                    log.debug("result data: {}", r);
                    assertThat(r.status()).isEqualTo(Status.PENDING_MODERATION);
                })
                .verifyComplete();
    }
}