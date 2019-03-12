package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    public void setup() {
        this.reactiveMongoTemplate.remove(Post.class).all()
            .subscribe(r -> log.debug("delete all posts: " + r), e -> log.debug("error: " + e), () -> log.debug("done"));
    }


    @Test
    public void testSavePost() {
        StepVerifier.create(this.postRepository.save(Post.builder().content("my test content").title("my test title").build()))
            .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
            .expectComplete()
            .verify();
    }


    @Test
    public void testSaveAndVerifyPost() {
        Post saved = this.postRepository.save(Post.builder().content("my test content").title("my test title").build()).block();
        assertThat(saved.getId()).isNotNull();
        assertThat(this.reactiveMongoTemplate.collectionExists(Post.class).block()).isTrue();
        assertThat(this.reactiveMongoTemplate.findById(saved.getId(), Post.class).block().getTitle()).isEqualTo("my test title");
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
