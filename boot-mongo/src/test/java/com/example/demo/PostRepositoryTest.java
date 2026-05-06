package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
//@ContextConfiguration(initializers = {MongodbContainerInitializer.class})
@Slf4j
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @TestConfiguration
    @Import(PostRepository.class)
    static class TestConfig {
    }

    @BeforeEach
    public void setup() {
        this.reactiveMongoTemplate.remove(Post.class).all()
                .subscribe(r -> log.debug("delete all posts: " + r), e -> log.debug("error: " + e),
                        () -> log.debug("done"));
    }

    @Test
    public void testSavePostAndFindByTitleContains() {
        this.postRepository.save(
                        Post.builder().content("my test content").title("my test title").build())
                .flatMapMany(p -> this.postRepository.findByTitleContains("test"))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testSavePost() {
        StepVerifier.create(this.postRepository.save(
                        Post.builder().content("my test content").title("my test title").build()))
                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

    @SneakyThrows
    @Test
    public void testSaveAndVerifyPost() {
        Post data = Post.builder().content("my test content").title("my test title").build();
        var latch = new CountDownLatch(1);
        var idRef = new AtomicReference<String>();
        this.postRepository.save(data)
                .subscribe(r -> {
                    log.debug("saved post: {}", r);
                    idRef.set(r.getId());
                    latch.countDown();
                });
        latch.await(500, TimeUnit.MILLISECONDS);

        var id = idRef.get();
        assertThat(id).isNotNull();

        this.reactiveMongoTemplate.collectionExists(Post.class)
                .as(StepVerifier::create)
                .consumeNextWith(r -> assertThat(r).isTrue())
                .verifyComplete();

        this.reactiveMongoTemplate.findById(id, Post.class)
                .as(StepVerifier::create)
                .consumeNextWith(r -> {
                    assertThat(r.getTitle()).isEqualTo("my test title");
                })
                .verifyComplete();
    }


    @Test
    public void testGetAllPost() {
        Post post1 = Post.builder().content("my test content").title("my test title").build();
        Post post2 = Post.builder().content("content of another post").title("another post title")
                .build();

        Flux<Post> allPosts = Flux.just(post1, post2)
                .flatMap(this.postRepository::save)
                .thenMany(this.postRepository.findAll().sort(Comparator.comparing(Post::getTitle)));

        StepVerifier.create(allPosts)
                .expectNextMatches(p -> p.getTitle().equals("another post title"))
                .expectNextMatches(p -> p.getTitle().equals("my test title"))
                .verifyComplete();
    }

}
