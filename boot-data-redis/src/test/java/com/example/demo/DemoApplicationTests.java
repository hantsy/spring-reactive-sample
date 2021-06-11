package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.Objects;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebTestClient
public class DemoApplicationTests {

    static DockerImageName redisDockerImageName = DockerImageName.parse("redis");

    @Container
    protected static final RedisContainer REDIS_DB_CONTAINER =
            new RedisContainer(redisDockerImageName).withExposedPorts(6379);

    static{
        REDIS_DB_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setMongoDbContainerURI(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.redis.host", REDIS_DB_CONTAINER::getHost);
        propertyRegistry.add("spring.redis.port", REDIS_DB_CONTAINER::getFirstMappedPort);
    }

    private static class RedisContainer extends GenericContainer<RedisContainer> {

        public RedisContainer(final DockerImageName dockerImageName) {
            super(dockerImageName);
        }
    }

    @Autowired
    WebTestClient client;

    @Test
    public void getAllPostsShouldBeOkWithAuthentication() {
        client
            .get()
            .uri("/posts/")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void getANoneExistedPostShouldReturn404() {
        client
            .get()
            .uri("/posts/ABC")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @Disabled
    public void createAPostNotAllowedWhenIsNotAuthorized() {
        client
            .post()
            .uri("/posts")
            .body(BodyInserters.fromValue(Post.builder().title("Post test").content("content of post test").build()))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Disabled
    public void deletePostsNotAllowedWhenIsNotAdmin() {
        client
            .mutate().filter(basicAuthentication("user", "password")).build()
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Disabled
    public void postCrudOperations() {
        int randomInt = new Random().nextInt();
        String title = "Post test " + randomInt;
        FluxExchangeResult<Void> postResult = client
            .mutate().filter(basicAuthentication("user", "password")).build()
            .post()
            .uri("/posts")
            .body(BodyInserters.fromValue(Post.builder().title(title).content("content of " + title).build()))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED)
            .returnResult(Void.class);

        URI location = postResult.getResponseHeaders().getLocation();
        assertThat(location).isNotNull();

        EntityExchangeResult<byte[]> getResult = client
            .get()
            .uri(location)
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.title").isEqualTo(title)
            .returnResult();

        String getPost = new String(Objects.requireNonNull(getResult.getResponseBody()));
        assertThat(getPost.contains(title)).isTrue();
    }

}
