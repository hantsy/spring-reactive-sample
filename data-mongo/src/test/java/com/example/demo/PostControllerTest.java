package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 *
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
class PostControllerTest {

    static DockerImageName mongoDockerImageName = DockerImageName.parse("mongo");

    @Container
    protected static final MongoDBContainer MONGO_DB_CONTAINER =
            new MongoDBContainer(mongoDockerImageName).withExposedPorts(27017);

    static {
        MONGO_DB_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setMongoDbContainerURI(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("mongo.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Autowired
    PostController ctrl;

    WebTestClient client;

    @BeforeAll
    public void setup() {
        this.client = WebTestClient
            .bindToController(this.ctrl)
            .configureClient()
            .build();
    }

    @Test
    void getAllPostsWillBeOk() {
        this.client
            .get()
            .uri("/posts")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(2);
    }
}
