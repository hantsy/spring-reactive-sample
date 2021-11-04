package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
@AutoConfigureWebTestClient
public class DemoApplicationTests {

  static DockerImageName redisDockerImageName = DockerImageName.parse("redis");

  @Container
  protected static final RedisContainer REDIS_DB_CONTAINER =
      new RedisContainer(redisDockerImageName).withExposedPorts(6379);

  static {
    REDIS_DB_CONTAINER.start();
  }

  @Autowired WebTestClient webTestClient;

  @DynamicPropertySource
  static void addsApplicationProperties(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.redis.host", REDIS_DB_CONTAINER::getHost);
    propertyRegistry.add("spring.redis.port", REDIS_DB_CONTAINER::getFirstMappedPort);
  }

  @Test
  @WithMockUser(username = "junit", password = "junit")
  public void getFavoritedWithoutAuthWillReturn200() {
    String slug = "testslug";
    webTestClient
        .get()
        .uri("/posts/" + slug + "/favorited")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  @WithMockUser
  public void postCrudOperations() {
    String slug = "testslug";
    webTestClient
        .post()
        .uri("/posts/" + slug + "/favorites")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);

    webTestClient
        .get()
        .uri("/posts/" + slug + "/favorited")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .jsonPath("$.favorited")
        .isEqualTo(true);

    webTestClient
        .get()
        .uri("/posts/" + slug + "/favorites")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .jsonPath("$[0]")
        .isEqualTo("user");

    webTestClient
        .get()
        .uri("/users/user/favorites")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .jsonPath("$[0]")
        .isEqualTo("testslug");

    webTestClient
        .delete()
        .uri("/posts/" + slug + "/favorites")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.NO_CONTENT);

    webTestClient
        .get()
        .uri("/posts/" + slug + "/favorited")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK)
        .expectBody()
        .jsonPath("$.favorited")
        .isEqualTo(false);
  }

  private static class RedisContainer extends GenericContainer<RedisContainer> {

    public RedisContainer(final DockerImageName dockerImageName) {
      super(dockerImageName);
    }
  }
}
