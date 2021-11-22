package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@Testcontainers
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class PostRepositoryWithDynamicPropertiesTest {

    @Container
    static MongoDBContainer container = new MongoDBContainer("mongo")
            .withStartupTimeout(Duration.ofSeconds(60));

    @DynamicPropertySource
    static void initMongoProperties(DynamicPropertyRegistry registry) {
        log.debug("container url: {}", container.getReplicaSetUrl("/blog"));
        log.debug("container host/port: {}/{}", container.getHost(), container.getFirstMappedPort());
        registry.add("spring.data.mongodb.uri", () -> container.getReplicaSetUrl("blog"));
    }

    @TestConfiguration()
    @Import(DataConfig.class)//enable auditing.
    static class TestConfig{}

    @Autowired
    PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        CountDownLatch latch = new CountDownLatch(1);
        this.posts.saveAll(
                        List.of(
                                Post.builder().content("my test content").title("my test title").build(),
                                Post.builder().content("content of another post").title("another post title").build()
                        )
                )
                .doOnComplete(latch::countDown)
                .subscribe();

        latch.await(5000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void testGetAllPost() {
        this.posts.findAll(Sort.by("title"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p.getTitle()).isEqualTo("another post title");
                    assertThat(p.getCreatedBy()).isEqualTo("hantsy");
                    assertThat(p.getCreatedAt()).isNotNull();
                })
                .consumeNextWith(p ->  assertThat(p.getTitle()).isEqualTo("my test title"))
                .verifyComplete();
    }

}
