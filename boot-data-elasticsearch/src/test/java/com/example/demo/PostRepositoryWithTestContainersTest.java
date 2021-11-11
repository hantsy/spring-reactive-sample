package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@DataElasticsearchTest
@SpringBootTest
@Testcontainers
public class PostRepositoryWithTestContainersTest {

    @Container
    static final ElasticsearchContainer elasticsearchContainer =
        new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.1")
            .withStartupTimeout(Duration.ofMinutes(5));

	static {
		elasticsearchContainer.start();
	}


    @DynamicPropertySource
    static void esProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }
	
	@BeforeAll
    void setUp() throws InterruptedException {
        // waiting so that posts are inserted
        TimeUnit.SECONDS.sleep(3);
    }

    @Autowired
    PostRepository posts;

    @Test
    void testAllPosts() {
        posts.findAll().sort(Comparator.comparing(Post::getTitle))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }


}