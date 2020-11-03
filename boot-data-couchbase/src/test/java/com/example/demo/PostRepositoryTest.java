package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
class PostRepositoryTest {


    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private static final String COUCHBASE_IMAGE_NAME = "couchbase:community";
        private static final String DEFAULT_IMAGE_NAME = "couchbase/server";

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            var dockerImageName = DockerImageName.parse(COUCHBASE_IMAGE_NAME).asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

            final CouchbaseContainer couchbaseContainer = new CouchbaseContainer(dockerImageName)
                    .withBucket(new BucketDefinition("demo").withPrimaryIndex(true));

            couchbaseContainer.start();

            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> couchbaseContainer.stop());
            TestPropertyValues.of(
                    "spring.couchbase.connection-string=" + couchbaseContainer.getConnectionString(),
                    "spring.couchbase.username=" + couchbaseContainer.getUsername(),
                    "spring.couchbase.password=" + couchbaseContainer.getPassword(),
                    "spring.data.couchbase.bucket-name=demo"
            )
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
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
