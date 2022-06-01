package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
// or exclude via @ImportAutoConfiguration
//@ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
// but @EnableAutoConfiguration(exclude=...) does not work,
// see: https://stackoverflow.com/questions/70047380/excluding-embededmongoautoconfiguration-failed-in-spring-boot-2-6-0
@ContextConfiguration(initializers = {MongodbContainerInitializer.class})
@Slf4j
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    public void setup() {
        this.reactiveMongoTemplate.remove(Post.class).all()
                .subscribe(r -> log.debug("delete all posts: " + r), e -> log.debug("error: " + e), () -> log.debug("done"));
    }

    @Test
    public void testSavePostAndFindByTitleContains() {
        this.postRepository.save(Post.builder().content("my test content").title("my test title").build())
                .flatMapMany(p -> this.postRepository.findByTitleContains("test"))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testSavePost() {
        StepVerifier.create(this.postRepository.save(Post.builder().content("my test content").title("my test title").build()))
                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testSaveAndVerifyPost() {
        Post saved = this.postRepository.save(Post.builder().content("my test content").title("my test title").build()).block();
        assertThat(saved.getId()).isNotNull();
        assertThat(this.reactiveMongoTemplate.collectionExists(Post.class).block()).isTrue();
        assertThat(this.reactiveMongoTemplate.findById(saved.getId(), Post.class).block().getTitle()).isEqualTo("my test title");
    }


    @SneakyThrows
    @Test
    public void testGetAllPost() {
        Post post1 = Post.builder().content("my test content").title("my test title").build();
        Post post2 = Post.builder().content("content of another post").title("another post title").build();

        var countDownLatch = new CountDownLatch(1);
        Flux.just(post1, post2)
                .flatMap(this.postRepository::save)
                .doOnTerminate(countDownLatch::countDown)
                .subscribe(
                        data -> log.debug("saved: {} ", data)
                );

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        var allPosts = this.postRepository.findAll(Sort.by((Sort.Direction.ASC), "title"));
        StepVerifier.create(allPosts)
                .expectNextMatches(p -> p.getTitle().equals("another post title"))
                .expectNextMatches(p -> p.getTitle().equals("my test title"))
                .verifyComplete();
    }

}
