package com.example.demo;


import com.example.demo.domain.Post;
import com.example.demo.repository.DataConfig;
import com.example.demo.repository.template.TemplatePostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

//@SpringBootTest
//
// @DataNeo4jTest does not work in Spring Data Neo4j 6.0 RC1 and Spring Boot 2.4.0-M3
// see: https://github.com/spring-projects/spring-boot/issues/23630
// a workaround is adding a `@Transactional(propagation = Propagation.NEVER)`
@SpringJUnitConfig(classes = {PostRepositoryTest.TestConfig.class, DataConfig.class})
@Transactional(propagation = Propagation.NEVER)
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@Slf4j
public class PostRepositoryTest {

    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5")
                    .withAdminPassword("passw0rd");
            neo4jContainer.start();
            log.info(" neo4jContainer.getBoltUrl():: {}", neo4jContainer.getBoltUrl());
            log.info(" neo4jContainer.getAdminPassword():: {}", neo4jContainer.getAdminPassword());
            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> neo4jContainer.stop());
            var env = configurableApplicationContext.getEnvironment();
            var props = env.getPropertySources();
            props.addFirst(
                    new MapPropertySource("testproperties",
                            Map.of(
                                    "spring.neo4j.uri", neo4jContainer.getBoltUrl(),
                                    "spring.neo4j.authentication.username", "neo4j",
                                    "spring.neo4j.authentication.password", neo4jContainer.getAdminPassword()
                            )
                    )
            );

        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = {
            DataConfig.class,
            Post.class
    })
    static class TestConfig {

    }


    @Autowired
    private TemplatePostRepository posts;

    @BeforeEach
    public void setup() throws IOException {
        log.debug("running setup.....,");

        var data = List.of("Post one", "Post two")
                .stream()
                .map(title -> Post.builder().title(title).content("The content of " + title).build())
                .collect(toList());

        var saveAllPosts = this.posts.saveAll(data);

        this.posts.deleteAll()
                .then()
                .thenMany(saveAllPosts)
                .log("[saved post]")
                .thenMany(this.posts.findAll())
                .log("[findAll]")
                .blockLast(Duration.ofSeconds(5));// to make the tests work
    }

    @AfterEach
    void teardown() {
        //this.posts.deleteAll();
    }

    @Test
    void testAllPosts() {
        posts.findAll().sort(Comparator.comparing(Post::getTitle))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }

    @Test
    void testFindByQuery() {
        posts.findByTitleContains("one").sort(Comparator.comparing(Post::getTitle))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .verifyComplete();
    }

}
