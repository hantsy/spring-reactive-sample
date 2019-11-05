package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.neo4j.springframework.boot.test.autoconfigure.data.DataNeo4jTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Optional;

@Testcontainers
@EnabledIfEnvironmentVariable(named = PostRepositoryTest.SYS_PROPERTY_NEO4J_VERSION, matches = "4\\.0.*")
@DataNeo4jTest
@ContextConfiguration(initializers = PostRepositoryTest.Initializer.class)
class PostRepositoryTest {

    private static final String SYS_PROPERTY_NEO4J_ACCEPT_COMMERCIAL_EDITION = "SDN_RX_NEO4J_ACCEPT_COMMERCIAL_EDITION";
    protected static final String SYS_PROPERTY_NEO4J_VERSION = "SDN_RX_NEO4J_VERSION";

    @Container
    private static Neo4jContainer<?> neo4jContainer =
            new Neo4jContainer<>("neo4j:" + System.getenv(SYS_PROPERTY_NEO4J_VERSION))
                    .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT",
                            Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_ACCEPT_COMMERCIAL_EDITION)).orElse("no"));
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


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "org.neo4j.driver.uri=" + neo4jContainer.getBoltUrl(),
                    "org.neo4j.driver.authentication.username=neo4j",
                    "org.neo4j.driver.authentication.password=" + neo4jContainer.getAdminPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}