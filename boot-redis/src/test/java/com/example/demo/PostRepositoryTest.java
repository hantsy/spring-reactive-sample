package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestDemoApplication.class)
@Slf4j
//@DataRedisTest works for blocking RedisRepository
class PostRepositoryTest {

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
        this.postRepository.findAll().sort(Comparator.comparing(Post::getTitle))
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.getTitle()).isEqualTo("Post one"))
                .consumeNextWith(it -> assertThat(it.getTitle()).isEqualTo("Post two"))
                .verifyComplete();

    }

}