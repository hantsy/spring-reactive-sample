package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.couchbase.DataCouchbaseTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@DataCouchbaseTest
@Testcontainers
@ActiveProfiles("test")
@Slf4j
class PostRepositoryWithTestcontainresTest {


    @TestConfiguration
    @Import(DataConfig.class)
    static class TestConfig {
    }

    private static final String COUCHBASE_IMAGE_NAME = "couchbase";
    private static final String DEFAULT_IMAGE_NAME = "couchbase/server";
    private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse(COUCHBASE_IMAGE_NAME)
            .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

    @Container
    final static CouchbaseContainer couchbaseContainer = new CouchbaseContainer(DEFAULT_IMAGE)
            .withCredentials("Administrator", "password")
            .withBucket(new BucketDefinition("demo").withPrimaryIndex(true))
            //.withStartupAttempts(30)
            .withStartupTimeout(Duration.ofSeconds(60));

    @DynamicPropertySource
    static void bindCouchbaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.couchbase.connection-string", couchbaseContainer::getConnectionString);
        registry.add("spring.couchbase.username", couchbaseContainer::getUsername);
        registry.add("spring.couchbase.password", couchbaseContainer::getPassword);
        registry.add("spring.data.couchbase.bucket-name", () -> "demo");
    }

    @Autowired
    private PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var countDownLatch = new CountDownLatch(1);
        this.posts
                .saveAll(
                        List.of(
                                Post.builder().title("Post one").content("content of post one").build(),
                                Post.builder().title("Post two").content("content of post two").build()
                        )
                )
                //.doOnTerminate(countDownLatch::countDown)
                .subscribe(data -> {
                    log.debug("saved data: {}", data);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Test
    void testLoadUsers() {
        this.posts.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post one"))
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post two"))
                //.expectNextCount(2)
                .verifyComplete();
    }

}
