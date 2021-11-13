package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@DataElasticsearchTest
@SpringBootTest
@Testcontainers
@Slf4j
// Testcontainers does not work well with per_class testinstance.
// see: https://stackoverflow.com/questions/61357116/exception-mapped-port-can-only-be-obtained-after-the-container-is-started-when/61358336#61358336
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PostRepositoryWithTestContainersTest {

    @Container
    static ElasticsearchContainer esContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.2")
            .withEnv("discovery.type", "single-node");

    @DynamicPropertySource
    static void esProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", esContainer::getHttpHostAddress);
    }

    @Autowired
    PostRepository posts;

    @Test
    void testAllPosts() {
        posts.findAll().sort(Comparator.comparing(Post::getTitle))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }


}