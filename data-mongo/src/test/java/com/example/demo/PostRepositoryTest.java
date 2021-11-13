package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.MongoConfig;
import com.example.demo.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MongoDBContainer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author hantsy
 */
@SpringJUnitConfig(classes = PostRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@Slf4j
public class PostRepositoryTest {

    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final MongoDBContainer container = new MongoDBContainer("mongo:4");
            container.start();
            log.info(" container.getFirstMappedPort():: {}", container.getFirstMappedPort());
            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> container.stop());
            var env = configurableApplicationContext.getEnvironment();
            var props = env.getPropertySources();
            props.addFirst(
                    new MapPropertySource("testproperties",
                            Map.of("mongo.uri", "mongodb://localhost:" + container.getFirstMappedPort())
                    )
            );

        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = {MongoConfig.class, Post.class})
    static class TestConfig {

    }

    @Autowired
    PostRepository posts;

    @Test
    public void testGetAllPosts() {
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
