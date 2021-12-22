package com.example.demo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
//@DataRedisTest works for blocking RedisRepository
class PostRepositoryTest {
    @Container
    static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setupRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getFirstMappedPort());
    }

    @Autowired
    PostRepository postRepository;

    @Autowired
    ReactiveRedisTemplate<String, Post> template;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        var latch = new CountDownLatch(1);
        this.postRepository.deleteAll().log()
                .doOnTerminate(latch::countDown)
                .subscribe();
        latch.await(5, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testGetAllPosts() {
        var data = Stream.of("one", "two")
                .map(it -> Post.builder().id(UUID.randomUUID().toString()).title("Post " + it).content("Content of post " + it).build())
                .toList();
        this.postRepository.saveAll(data).block(Duration.ofMillis(5000));
        this.postRepository.findAll()
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.getTitle()).isEqualTo("Post one"))
                .consumeNextWith(it -> assertThat(it.getTitle()).isEqualTo("Post two"))
                .verifyComplete();

    }

}