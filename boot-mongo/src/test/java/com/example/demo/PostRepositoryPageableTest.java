package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
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

@DataMongoTest
@Import({PostRepository.class, ContainersConfig.class})
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
        List<Post> data = IntStream.range(1, 50)
                .mapToObj(
                        i -> Post.builder().content("my test content of #" + i).title("my test title #" + i)
                                .build()
                )
                .collect(Collectors.<Post>toList());
        var latch = new CountDownLatch(1);
        this.postRepository.saveAll(data)
                .subscribe(result -> {
                    log.debug("saved data: {}", result);
                    latch.countDown();
                });
        latch.await(500, TimeUnit.MILLISECONDS);
    }


    @Test
    public void testFindByTitleContainsPageable() {
        this.postRepository.findByTitleContains("title")
                .sort(comparing(Post::getTitle))
                .skip(0)
                .take(10)
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();

        this.postRepository.findByTitleContains("title",
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title")))
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    @Disabled
    public void testFindByKeyword() {
        this.postRepository.findByKeyword("title")
                .skip(0)
                .take(10)
                .log()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }

}
