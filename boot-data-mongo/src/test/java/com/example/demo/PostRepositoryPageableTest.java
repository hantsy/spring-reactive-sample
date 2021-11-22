package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@TestPropertySource(properties = {
        "logging.level.org.springframework.data.mongodb.core.ReactiveMongoTemplate=DEBUG",
        "logging.level.com.example.demo=DEBUG"
})
@Slf4j
public class PostRepositoryPageableTest {

    @Autowired
    PostRepository postRepository;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        CountDownLatch latch = new CountDownLatch(1);
        List<Post> data = IntStream.range(1, 50)
                .mapToObj(
                        i -> Post.builder().content("my test content of #" + i).title("my test title #" + i).build()
                )
                .collect(Collectors.<Post>toList());
        this.postRepository.saveAll(data)
                .doOnComplete(latch::countDown)
                .subscribe();

        latch.await(5000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void testFindByTitleContainsPageable() {
        this.postRepository.findByTitleContains("title")
                .sort(comparing(PostSummary::getTitle))
                .skip(0)
                .take(10)
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();

        this.postRepository.findByTitleContains("title", PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title")))
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    public void testFindByKeyword() {
        this.postRepository.findByKeyword(".*title.*")
                .skip(0)
                .take(10)
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }

}
