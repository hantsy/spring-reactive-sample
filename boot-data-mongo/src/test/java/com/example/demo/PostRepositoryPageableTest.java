package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@DataMongoTest
@ContextConfiguration(initializers = {MongodbContainerInitializer.class})
@TestPropertySource(properties = {
        "logging.level.org.springframework.data.mongodb.core.ReactiveMongoTemplate=DEBUG",
        "logging.level.com.example.demo=DEBUG"
})
@Slf4j
public class PostRepositoryPageableTest {

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    public void setup() {
        List<Post> data = IntStream.range(1, 50)
                .mapToObj(
                        i -> Post.builder().content("my test content of #" + i).title("my test title #" + i).build()
                )
                .collect(Collectors.<Post>toList());
        this.postRepository.saveAll(data).blockLast(Duration.ofSeconds(5));
    }


    @Test
    public void testFindByTitleContainsPageable() {
        this.postRepository.findByTitleContains("title")
                .skip(0)
                .take(10)
                .sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()))
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

}
