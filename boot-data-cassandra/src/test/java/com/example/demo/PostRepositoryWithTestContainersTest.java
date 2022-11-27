package com.example.demo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataCassandraTest
@Testcontainers
@Slf4j
public class PostRepositoryWithTestContainersTest {

    @Container
    static CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra")
            .withInitScript("init.cql")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void bindCassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.keyspace-name", () -> "demo");
        registry.add("spring.cassandra.contact-points", () -> "localhost:" + cassandraContainer.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.schema-action", () -> "RECREATE");
    }


    @Autowired
    private PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.posts.deleteAll()
                .then()
                .thenMany(
                        posts.saveAll(
                                List.of(Post.builder().title("test").content("content of test title").build(),
                                        Post.builder().title("test2").content("content of test2 title").build()
                                )
                        )
                )
                .log()
                .subscribe(data -> {
                            log.debug("data: {}", data);
                            latch.countDown();
                        }
                );
        latch.await();
    }

    @Test
    void testAllPosts() {

        posts.findAll().sort(Comparator.comparing(Post::getTitle))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("test", p.getTitle()))
                .consumeNextWith(p -> assertEquals("test2", p.getTitle()))
                .verifyComplete();
    }


}