package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

@DataMongoTest
@Slf4j
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        CountDownLatch latch = new CountDownLatch(1);
        this.reactiveMongoTemplate.remove(Post.class).all()
                .doOnTerminate(latch::countDown)
                .subscribe(r -> log.debug("delete all posts: " + r), e -> log.debug("error: " + e), () -> log.debug("done"));

        latch.await(5000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testGetAllPost() {
        Post post1 = Post.builder().content("my test content").title("my test title").build();
        Post post2 = Post.builder().content("content of another post").title("another post title").build();

        Flux<Post> allPosts = this.postRepository
                .saveAll(asList(post1, post2))
                .thenMany(this.postRepository.findAll(Sort.by((Sort.Direction.ASC), "title")));

        StepVerifier.create(allPosts)
                .expectNextMatches(p -> p.getTitle().equals("another post title"))
                .expectNextMatches(p -> p.getTitle().equals("my test title"))
                .verifyComplete();


        this.postRepository.findAll(QPost.post.title.containsIgnoreCase("my"))
                .as(StepVerifier::create)
                .expectNextMatches(p -> p.getTitle().equals("my test title"))
                .verifyComplete();
    }

}
