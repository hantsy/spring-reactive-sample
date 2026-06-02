package com.example.demo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.elasticsearch.test.autoconfigure.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@Import(TestcontainersConfiguration.class)
@Slf4j
class PostRepositoryTest {


    @Autowired
    PostRepository posts;

    @Autowired
    ReactiveElasticsearchOperations template;

    @SneakyThrows
    @BeforeEach
    void setup() {
        var latch = new CountDownLatch(1);
        this.posts.deleteAll()
                .thenMany(
                        this.posts.saveAll(
                                List.of(
                                        Post.builder().title("Post one").content("content of post one").build(),
                                        Post.builder().title("Post two").content("content of post two").build()
                                ),
                                RefreshPolicy.IMMEDIATE
                        )
                )
                .doOnError(ex -> log.error("failed to seed test data", ex))
                .doFinally(signal -> {
                    log.debug("seeding finished with signal: {}", signal);

                })
                .doOnComplete(() -> {
                    log.debug("completing...");
                })
                .subscribe(data -> {
                    log.debug("saved data: {}", data);
                    latch.countDown();
                });

        boolean seeded = latch.await(5000, MILLISECONDS);
        assertThat(seeded).as("Timed out waiting for Elasticsearch seed data").isTrue();
        log.debug("the sample data is ready ...");
    }

    @Test
    void testLoadData() {
        this.posts.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post one"))
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post two"))
                .verifyComplete();
    }

    @Test
    void testSavedPostTitles() {
        this.posts.findAll()
                .map(Post::getTitle)
                .log()
                .collectList()
                .as(StepVerifier::create)
                .consumeNextWith(titleList -> assertThat(titleList)
                        .containsExactlyInAnyOrder("Post one", "Post two"))
                .verifyComplete();
    }

    @Test
    void testLoadData_byTemplate() {
        this.template.search(Query.findAll(), Post.class)
                .map(SearchHit::getContent)
                .map(Post::getTitle)
                .log()
                .collectList()
                .as(StepVerifier::create)
                .consumeNextWith(titleList -> assertThat(titleList)
                        .containsExactlyInAnyOrder("Post one", "Post two"))
                .verifyComplete();
    }

}