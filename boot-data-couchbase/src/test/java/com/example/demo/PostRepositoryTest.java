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
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@DataCouchbaseTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Slf4j
class PostRepositoryTest {

    @TestConfiguration
    @Import(DataConfig.class)
    static class TestConfig {
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
                    countDownLatch.countDown();
                    ;
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
