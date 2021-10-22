package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@Slf4j
class PostRepositoryWithTestContainersTest {

  @Container
  static ElasticsearchContainer elasticsearchContainer =
      new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.12.1")
          .withStartupTimeout(Duration.ofMinutes(5));

  @DynamicPropertySource
  static void esProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.data.elasticsearch.client.reactive.endpoints",
        elasticsearchContainer::getHttpHostAddress);
    registry.add("spring.elasticsearch.rest.uris", elasticsearchContainer::getHttpHostAddress);
  }

  @Autowired PostRepository postRepository;

  @Test
  void testAllPosts() throws InterruptedException {
    // waiting so that posts are inserted
    TimeUnit.SECONDS.sleep(3);
    postRepository
        .findAll()
        .sort(Comparator.comparing(Post::getTitle))
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
        .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
        .verifyComplete();
  }
}
