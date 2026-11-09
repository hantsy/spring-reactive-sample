package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
public class PostRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));

    @Autowired
    PostRepository posts;

    @BeforeEach
    public void setup() {
        this.posts.deleteAll().block();
    }

    @Test
    public void testSaveAndFindById() {
        this.posts.save(Post.of("test", "content"))
                .flatMap(post -> this.posts.findById(post.id()))
                .as(StepVerifier::create)
                .consumeNextWith(post -> {
                    assertThat(post.title()).isEqualTo("test");
                    assertThat(post.status()).isEqualTo(Status.DRAFT);
                })
                .verifyComplete();
    }

    @Test
    public void testSaveAll() {
        this.posts.saveAll(List.of(Post.of("test", "content"), Post.of("test1", "content1")))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }
}
