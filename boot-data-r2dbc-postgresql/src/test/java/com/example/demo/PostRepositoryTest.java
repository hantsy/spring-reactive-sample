package com.example.demo;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest
public class PostRepositoryTest {

    @Autowired
    DatabaseClient client;

    @Autowired
    PostRepository posts;

    @Test
    public void testDatabaseClientExisted() {
        assertNotNull(client);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(posts);
    }

    @Test
    public void testInsertAndQuery() {
        this.client.insert()
                .into("posts")
                //.nullValue("id", Integer.class)
                .value("title", "testtitle")
                .value("content", "testcontent")
                .then();

        this.posts.findByTitleContains("testtitle")
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("testtitle", p.getTitle()))
                .verifyComplete();

    }
}
