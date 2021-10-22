package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PostRepositoryWithDynamicPropertiesTest {

    private static final String COUCHBASE_IMAGE_NAME = "couchbase:community";
    private static final String DEFAULT_IMAGE_NAME = "couchbase/server";
    private final static DockerImageName DEFAULT_IMAGE = DockerImageName.parse(COUCHBASE_IMAGE_NAME)
            .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

    @Container
    final static CouchbaseContainer couchbaseContainer = new CouchbaseContainer(DEFAULT_IMAGE)
            .withBucket(new BucketDefinition("demo").withPrimaryIndex(true));

    @DynamicPropertySource
    static void bindCouchbaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.couchbase.connection-string", couchbaseContainer::getConnectionString);
        registry.add("spring.couchbase.username", couchbaseContainer::getUsername);
        registry.add("spring.couchbase.password", couchbaseContainer::getPassword);
        registry.add("spring.data.couchbase.bucket-name", () -> "demo");
    }

    @Autowired
    private PostRepository posts;

    @Test
    void testLoadUsers() {
        this.posts.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post one"))
                .consumeNextWith(user -> assertThat(user.getTitle()).isEqualTo("Post two"))
                //.expectNextCount(2)
                .verifyComplete();
    }

}
