package com.example.demo.domain;

import com.example.demo.*;
import com.redis.testcontainers.RedisContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.redis.testcontainers.RedisContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisContainer.DEFAULT_TAG;

@Slf4j
@SpringJUnitConfig(classes = {PostRepositoryTest.TestConfig.class})
@ContextConfiguration(
        initializers = {
                PostRepositoryTest.PostgreSQLContainerInitializer.class,
                PostRepositoryTest.RedisContainerInitializer.class,
        }
)
public class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var counter = new CountDownLatch(1);
        this.posts.deleteAll()
                .doOnTerminate(counter::countDown)
                .subscribe(v -> log.debug("deleted all posts"));
        counter.await(500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSaveAll() {
        var data = Post.of("test", "content");
        var post = posts.save(data).block();

        log.debug("Calling findById at the first time.");
        var firstCalled = posts.findById(post.id()).block();
        log.debug("The first call of saved post:{}", firstCalled);

        log.debug("Calling findById at the second time.");
        var secondCalled = posts.findById(post.id()).block();
        log.debug("The second call of saved post:{}", secondCalled);

        log.debug("deleting post by id...");
        posts.deleteById(post.id()).block();
        log.debug("after deleteById.");

        log.debug("call findAll which is not cached.");
        posts.findAll().subscribe(p -> log.debug("get saved post: {}", p));
    }

    //see: https://github.com/testcontainers/testcontainers-java/discussions/4841
    static class PostgreSQLContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12")
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("init.sql"),
                        "/docker-entrypoint-initdb.d/init.sql"
                );

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            postgreSQLContainer.start();

            log.info(" container.getFirstMappedPort():: {}", postgreSQLContainer.getFirstMappedPort());
            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    postgreSQLContainer.stop();
                }
            });

            context.getEnvironment().getPropertySources()
                    .addFirst(
                            new MapPropertySource("r2dbcProperties",
                                    Map.of("r2dbc.host", postgreSQLContainer.getHost(),
                                            "r2dbc.port", postgreSQLContainer.getFirstMappedPort(),
                                            "r2dbc.database", postgreSQLContainer.getDatabaseName(),
                                            "r2dbc.username", postgreSQLContainer.getUsername(),
                                            "r2dbc.password", postgreSQLContainer.getPassword()
                                    )
                            )
                    );
        }
    }

    static class RedisContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        final RedisContainer redisContainer = new RedisContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            redisContainer.start();
            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    redisContainer.stop();
                }
            });

            context.getEnvironment().getPropertySources()
                    .addFirst(
                            new MapPropertySource("redisProperties",
                                    Map.of(
                                            "redis.host", redisContainer.getHost(),
                                            "redis.port", redisContainer.getFirstMappedPort()
                                    )
                            )
                    );
        }
    }

    @Configuration
    @Import(value = {
            DataR2dbcConfig.class,
            CacheConfig.class,
            RedisConfig.class
    })
    static class TestConfig {
    }
}
