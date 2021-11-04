package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataNeo4jTest
@Slf4j
class PostRepositoryTest extends Neo4jContainerSetUp {

  @Autowired private PostRepository postRepository;

  @BeforeEach
  public void setup() {
    log.debug("running setup.....,");
    this.postRepository
        .deleteAll()
        .thenMany(testSaveMethod())
        .log()
        .thenMany(testFoundMethod())
        .log()
        .blockLast();
  }

  private Flux<Post> testSaveMethod() {
    var data =
        Stream.of("Post one", "Post two")
            .map(title -> Post.builder().title(title).content("The content of " + title).build())
            .collect(Collectors.toList());
    return Flux.fromIterable(data).flatMap(it -> this.postRepository.save(it));
  }

  private Flux<Post> testFoundMethod() {
    return this.postRepository.findByTitleContains("one");
  }

  @Test
  void testAllPosts() {
    postRepository
        .findAll()
        .sort(Comparator.comparing(post -> post.getTitle()))
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
        .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
        .verifyComplete();
  }

  @TestConfiguration
  @Import(PostRepository.class)
  static class TestConfig {}
}
