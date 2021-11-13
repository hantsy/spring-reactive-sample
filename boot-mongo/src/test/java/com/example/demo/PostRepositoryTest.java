package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

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
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @TestConfiguration
  @Import(PostRepository.class)
  static class TestConfig{}

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

  @Test
  public void testSaveAndVerifyPost() {
    Post saved = this.postRepository.save(
        Post.builder().content("my test content").title("my test title").build()).block();
    assertThat(saved.getId()).isNotNull();
    assertThat(this.reactiveMongoTemplate.collectionExists(Post.class).block()).isTrue();
    assertThat(this.reactiveMongoTemplate.findById(saved.getId(), Post.class).block()
        .getTitle()).isEqualTo("my test title");
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
