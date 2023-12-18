package com.example.demo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
//@AutoConfigureDataElasticsearch
@Testcontainers
@ActiveProfiles("test")
@Slf4j
// Testcontainers does not work well with per_class testinstance.
// see: https://stackoverflow.com/questions/61357116/exception-mapped-port-can-only-be-obtained-after-the-container-is-started-when/61358336#61358336
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PostRepositoryWithTestContainersTest {

    @Container
    static ElasticsearchContainer esContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.3")
            .withEnv(Map.of("xpack.security.enabled", "false"));

    @DynamicPropertySource
    static void esProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", esContainer::getHttpHostAddress);
    }

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
                .doOnTerminate(() -> {
                    log.debug("terminating...");
                    //countDownLatch.countDown();
                })
                .doOnComplete(() -> {
                    log.debug("completing...");
                    countDownLatch.countDown();
                })
                .subscribe(data -> {
                    log.debug("saved data: {}", data);
                });

        countDownLatch.await(5000, MILLISECONDS);
        log.debug("the sample data is ready ...");
    }

    @Test
    void testDatabaseIsRunning() {
        assertThat(esContainer.isRunning()).isTrue();
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