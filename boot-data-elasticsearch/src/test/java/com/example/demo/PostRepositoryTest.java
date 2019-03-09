package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.Query;
import reactor.test.StepVerifier;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class PostRepositoryTest {

    @Autowired
    private ReactiveElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    public void setup() {
        this.elasticsearchTemplate.deleteBy(Query.findAll(), Post.class);
    }

    @Test
    public void testSavePost() {
        StepVerifier.create(this.postRepository.save(Post.builder().content("my test content").title("my test title").build()))
            .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
            .expectComplete()
            .verify();
    }

    @Test
    public void testSaveAllPosts() {
        Post post1 = Post.builder().content("my test content").title("my test title").build();
        Post post2 = Post.builder().content("content of another post").title("another post title").build();

        this.postRepository.saveAll(asList(post1, post2))
            .as(StepVerifier::create)
            .consumeNextWith(it ->
                assertThat(it.getTitle()).isEqualTo("another post title")
            )
            .consumeNextWith(it ->
                assertThat(it.getTitle()).isEqualTo("my test title")
            )
            .verifyComplete();
    }

}
