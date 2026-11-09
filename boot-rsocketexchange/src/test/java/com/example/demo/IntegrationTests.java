package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class IntegrationTests {

    @Autowired
    PostClient client;

    @Test
    public void testCreateAndGet() {
        var data = Post.of("declarative client", "content of the declarative client post");

        this.client.create(data)
                .flatMap(id -> this.client.get(id))
                .as(StepVerifier::create)
                .consumeNextWith(post -> {
                    assertThat(post.title()).isEqualTo(data.title());
                    assertThat(post.content()).isEqualTo(data.content());
                    assertThat(post.status()).isEqualTo(Status.DRAFT);
                })
                .verifyComplete();
    }

    @Test
    public void testUpdateAndDelete() {
        var data = Post.of("before update", "content");

        this.client.create(data)
                .flatMap(id -> this.client.update(id, Post.of("after update", "updated content", Status.PUBLISHED))
                        .thenReturn(id))
                .flatMap(id -> this.client.get(id))
                .as(StepVerifier::create)
                .consumeNextWith(post -> {
                    assertThat(post.title()).isEqualTo("after update");
                    assertThat(post.status()).isEqualTo(Status.PUBLISHED);
                })
                .verifyComplete();

        var id = this.client.create(data).block();
        this.client.delete(id)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        this.client.get(id)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    public void testTitleContains() {
        var keyword = "rsocket-" + UUID.randomUUID();
        var data = Post.of("about " + keyword, "content");

        this.client.create(data)
                .thenMany(this.client.titleContains(keyword))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }
}