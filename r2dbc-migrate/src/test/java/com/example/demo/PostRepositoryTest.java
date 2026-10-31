package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import name.nkonev.r2dbc.migrate.autoconfigure.R2dbcMigrateAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest()
@Slf4j
@Import(TestcontainersConfiguration.class)
@ImportAutoConfiguration(R2dbcMigrateAutoConfiguration.class)
public class PostRepositoryTest {

    @Autowired
    R2dbcEntityTemplate template;

    @Autowired
    PostRepository posts;

    @Test
    public void testDatabaseClientExisted() {
        assertNotNull(template);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(posts);
    }

    @Test
    public void testInsertAndQuery() {
        posts.findAll().log()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

}
