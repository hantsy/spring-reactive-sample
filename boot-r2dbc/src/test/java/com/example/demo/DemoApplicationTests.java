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

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Slf4j
class DemoApplicationTests {

    @Autowired
    PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.posts.deleteAll()
                .doOnTerminate(latch::countDown)
                .subscribe(
                        data -> log.info("clean database: {} deleted.", data)
                );
        latch.await(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSaveAll() {
        var data = new Post( null ,"test", "content", Status.PENDING_MODERATION, null );
        var data1 = Post.of("test1", "content1");

        var result = posts.saveAll(List.of(data, data1)).log("[Generated result]")
                .doOnNext(id -> log.info("generated id: {}", id));

        assertThat(result).isNotNull();
        result.as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(posts.countByStatus())
                .consumeNextWith(r -> {
                    log.info("data: {}", r);
                    assertThat(r.get("status")).isEqualTo(Status.DRAFT);
                })
                .consumeNextWith(r -> {
                    log.info("data: {}", r);
                    assertThat(r.get("cnt")).isEqualTo(1L);
                    assertThat(r.get("status")).isEqualTo(Status.PENDING_MODERATION);
                })
                .verifyComplete();
    }

    //see: https://stackoverflow.com/questions/64374730/java-r2dbc-client-execute-sql-and-use-returned-id-for-next-execute/64409363#64409363
    @Test
    public void testInsertAndQuery() {
        var data = new Post( null ,"test", "content", Status.PENDING_MODERATION, null );
        this.posts.save(data)
                .flatMap(id -> this.posts.findById(id))
                .as(StepVerifier::create)
                .consumeNextWith(r -> {
                    log.info("result data: {}", r);
                    assertThat(r.status()).isEqualTo(Status.PENDING_MODERATION);
                })
                .verifyComplete();
    }

    @Test
    public void testInsertAndDelete() {
        var data = new Post( null ,"test", "content", Status.PENDING_MODERATION, null );
        this.posts.save(data)
                .flatMap(id -> this.posts.deleteAllById(List.of(id)))
                .as(StepVerifier::create)
                .consumeNextWith(r -> {
                    log.info("deleted result: {}", r);
                    assertThat(r).isGreaterThan(0);
                })
                .verifyComplete();
    }

}
