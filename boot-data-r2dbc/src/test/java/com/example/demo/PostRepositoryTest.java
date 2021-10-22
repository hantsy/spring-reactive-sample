package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// see: https://github.com/spring-projects-experimental/spring-boot-r2dbc/issues/68
@DataR2dbcTest
public class PostRepositoryTest {

    @Autowired
    DatabaseClient client;

    @Autowired
    PostRepository postRepository;

    @Test
    public void testDatabaseClientExisted() {
        assertNotNull(client);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(postRepository);
    }

    @Test
    public void testInsertAndQuery() {
        this.client.sql(() -> "insert into posts (title, content) values ('testtitle', 'testcontent')")
                .then().block(Duration.ofSeconds(5));

        this.postRepository.findByTitleContains("testtitle")
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("testtitle", p.getTitle()))
                .verifyComplete();

    }
}
