package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest
@ActiveProfiles("tc")
class TestContainersPostRepositoryTest extends MySQLContainerConfig {

  @Autowired DatabaseClient client;

  @Autowired PostRepository posts;

  @Test
  void testDatabaseClientExisted() {
    assertNotNull(client);
  }

  @Test
  void testPostRepositoryExisted() {
    assertNotNull(posts);
  }

  @Test
  void existedOneItemInPosts() {
    assertThat(this.posts.count().block()).isGreaterThanOrEqualTo(1);
  }

  @Test
  void testInsertAndQuery() {
    this.client
        .sql("insert into posts (title , content ) values (:title,:content)")
        .bind("title", "mytesttitle")
        .bind("content", "testcontent")
        .then()
        .block(Duration.ofSeconds(5));

    this.posts
        .findByTitleContains("%testtitle")
        .take(1)
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("mytesttitle", p.getTitle()))
        .verifyComplete();
  }
}
