package com.example.demo;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;
import wiremock.com.fasterxml.jackson.databind.DeserializationFeature;
import wiremock.com.fasterxml.jackson.databind.SerializationFeature;
import wiremock.com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringJUnitConfig(
        classes = {
                ClientConfig.class,
                Jackson2ObjectMapperConfig.class
        }
)
@WireMockTest(httpPort = 8080)
public class PostClientTest {

    static {
        wiremock.com.fasterxml.jackson.databind.ObjectMapper wireMockObjectMapper = Json.getObjectMapper();
        wireMockObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        wireMockObjectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        wireMockObjectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        JavaTimeModule module = new JavaTimeModule();
        wireMockObjectMapper.registerModule(module);
    }

    @Autowired
    PostClient postClient;

    @Autowired
    ObjectMapper objectMapper;

    @SneakyThrows
    @BeforeEach
    public void setup() {
    }

    @SneakyThrows
    @Test
    public void testGetAllPosts() {
        var data = List.of(
                new Post(UUID.randomUUID(), "title1", "content1", Status.DRAFT, LocalDateTime.now()),
                new Post(UUID.randomUUID(), "title2", "content2", Status.PUBLISHED, LocalDateTime.now())
        );
        stubFor(get("/posts")
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withResponseBody(Body.fromJsonBytes(Json.toByteArray(data)))
                )
        );

        postClient.allPosts()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        verify(getRequestedFor(urlEqualTo("/posts"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @SneakyThrows
    @Test
    public void testGetPostById() {
        var id = UUID.randomUUID();
        var data = new Post(id, "title1", "content1", Status.DRAFT, LocalDateTime.now());

        stubFor(get("/posts/" + id)
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withResponseBody(Body.fromJsonBytes(Json.toByteArray(data)))
                )
        );

        postClient.getById(id)
                .as(StepVerifier::create)
                .consumeNextWith(
                        post -> {
                            assertThat(post.id()).isEqualTo(id);
                            assertThat(post.title()).isEqualTo(data.title());
                            assertThat(post.content()).isEqualTo(data.content());
                            assertThat(post.status()).isEqualTo(data.status());
                            assertThat(post.createdAt()).isEqualTo(data.createdAt());
                        }
                )
                .verifyComplete();

        verify(getRequestedFor(urlEqualTo("/posts/" + id))
                .withHeader("Accept", equalTo("application/json"))
        );
    }

    @SneakyThrows
    @Test
    public void testCreatePost() {
        var id = UUID.randomUUID();
        var data = new Post(null, "title1", "content1", Status.DRAFT, null);

        stubFor(post("/posts")
                .willReturn(
                        aResponse()
                                .withHeader("Location", "/posts/" + id)
                                .withStatus(201)
                )
        );

        postClient.save(data)
                .as(StepVerifier::create)
                .consumeNextWith(
                        entity -> {
                            assertThat(entity.getHeaders().getLocation().toString()).isEqualTo("/posts/" + id);
                            assertThat(entity.getStatusCode().value()).isEqualTo(201);
                        }
                )
                .verifyComplete();

        verify(postRequestedFor(urlEqualTo("/posts"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(Json.write(data)))
        );
    }

    @SneakyThrows
    @Test
    public void testUpdatePost() {
        var id = UUID.randomUUID();
        var data = new Post(null, "title1", "content1", Status.DRAFT, null);

        stubFor(put("/posts/" + id)
                .willReturn(
                        aResponse()
                                .withStatus(204)
                )
        );

        postClient.update(id, data)
                .as(StepVerifier::create)
                .consumeNextWith(
                        entity -> assertThat(entity.getStatusCode().value()).isEqualTo(204)
                )
                .verifyComplete();

        verify(putRequestedFor(urlEqualTo("/posts/" + id))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(Json.write(data)))
        );
    }

    @SneakyThrows
    @Test
    public void testDeletePostById() {
        var id = UUID.randomUUID();
        stubFor(delete("/posts/" + id)
                .willReturn(
                        aResponse()
                                .withStatus(204)
                )
        );

        postClient.delete(id)
                .as(StepVerifier::create)
                .consumeNextWith(
                        entity -> assertThat(entity.getStatusCode().value()).isEqualTo(204)
                )
                .verifyComplete();

        verify(deleteRequestedFor(urlEqualTo("/posts/" + id)));
    }
}
