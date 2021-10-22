package com.example.demo;


import com.example.demo.config.CassandraContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataCassandraTest
@Slf4j
class PostRepositoryWithTestContainersTest extends CassandraContainerConfig {

    @Autowired
    private PostRepository posts;

    @BeforeEach
    void setup() {
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
                .blockLast(Duration.ofSeconds(5));
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