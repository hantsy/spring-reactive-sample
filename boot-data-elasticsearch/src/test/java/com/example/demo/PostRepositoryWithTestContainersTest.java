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
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@ActiveProfiles("test")
@Import(ContainersConfig.class)
@Slf4j
// Testcontainers does not work well with per_class testinstance.
// see: https://stackoverflow.com/questions/61357116/exception-mapped-port-can-only-be-obtained-after-the-container-is-started-when/61358336#61358336
class PostRepositoryWithTestContainersTest {


    @Autowired
    PostRepository posts;

    @Autowired
    ReactiveElasticsearchOperations template;

    @SneakyThrows
    @BeforeEach
    void setup() {
        var countDownLatch = new CountDownLatch(1);
        this.posts.deleteAll().block(Duration.ofMillis(1000));

        this.posts.saveAll(
                        List.of(
                                Post.builder().title("Post one").content("content of post one").build(),
                                Post.builder().title("Post two").content("content of post two").build()
                        ),
                        RefreshPolicy.IMMEDIATE
                )
                .doOnError(ex -> log.error("failed to seed test data", ex))
                .doFinally(signal -> {
                    log.debug("seeding finished with signal: {}", signal);
                    countDownLatch.countDown();
                })
                .doOnComplete(() -> {
                    log.debug("completing...");
                })
                .subscribe(data -> {
                    log.debug("saved data: {}", data);
                });

        boolean seeded = countDownLatch.await(5000, MILLISECONDS);
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