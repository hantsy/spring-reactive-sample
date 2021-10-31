package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = MongoConfig.class)
@Slf4j
class PostRepositoryTest {

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
    PostRepository posts;

    @Test
    void testGetAllPosts() {
        this.posts.deleteAll()
                .thenMany(
                        Flux.just("Post one", "Post two")
                                .flatMap(
                                        title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
                                )
                )
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertTrue(p.getTitle().contains("one")))
                .consumeNextWith(p -> assertTrue(p.getTitle().contains("two")))
                .expectComplete()
                .verify();
    }

}
