package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest
@Slf4j
public class PostRepositoryTest {

    @Autowired
    R2dbcEntityTemplate client;

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
        var data = Post.builder().title("testtitle").content("testcontent").build();
        this.client.insert(data)
                .then().block(Duration.ofSeconds(5));

        this.posts.findByTitleContains("testtitle")
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                            log.info("saved post: {}", p);
                            assertEquals("testtitle", p.getTitle());
                            assertNotNull(p.getCreatedAt());
                            assertNotNull(p.getUpdatedAt());
                            assertThat(p.getCreatedBy()).isEqualTo("hantsy");
                            assertThat(p.getUpdatedBy()).isEqualTo("hantsy");
                        }
                )
                .verifyComplete();

    }
}
