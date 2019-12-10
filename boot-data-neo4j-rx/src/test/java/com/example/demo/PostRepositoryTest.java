package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.springframework.boot.test.autoconfigure.data.DataNeo4jTest;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.io.IOException;

@DataNeo4jTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;


    @BeforeEach
    void setup() throws IOException {
    }

    @Test
    void testAllPostsCount() {
        int expectedPostsCount = 2;
        StepVerifier.create(postRepository.findAll())
                .expectNextCount(expectedPostsCount)
                .verifyComplete();
    }

}