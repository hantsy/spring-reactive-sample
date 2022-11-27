package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@ActiveProfiles("test")
@Slf4j
class PostRepositoryTest {

    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        private static final String COUCHBASE_IMAGE_NAME = "couchbase";
        private static final String DEFAULT_IMAGE_NAME = "couchbase/server";
        private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse(COUCHBASE_IMAGE_NAME)
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final CouchbaseContainer couchbaseContainer = new CouchbaseContainer(DEFAULT_IMAGE)
                    .withCredentials("Administrator", "password")
                    .withBucket(new BucketDefinition("demo").withPrimaryIndex(true))
                    .withStartupTimeout(Duration.ofSeconds(60));

            couchbaseContainer.start();

            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> couchbaseContainer.stop());
            TestPropertyValues.of(
                            "spring.couchbase.connection-string=" + couchbaseContainer.getConnectionString(),
                            "spring.couchbase.username=" + couchbaseContainer.getUsername(),
                            "spring.couchbase.password=" + couchbaseContainer.getPassword(),
                            "spring.data.couchbase.bucket-name=demo"
                    )
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
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
                .subscribe(data -> {
                    log.debug("saved data: {}", data);
                    countDownLatch.countDown();;
                });
        countDownLatch.await(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Test
    void testLoadUsers() {
        this.posts.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(user -> {
                    assertThat(user.getTitle()).isEqualTo("Post one");
                    //verify data auditing
                    assertThat(user.getCreatedBy()).isEqualTo("hantsy");
                    assertThat(user.getCreatedAt()).isNotNull();
                })
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post two"))
                //.expectNextCount(2)
                .verifyComplete();
    }

}
