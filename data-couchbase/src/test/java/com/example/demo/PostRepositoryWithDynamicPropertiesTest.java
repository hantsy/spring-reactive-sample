package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Application.class)
@Testcontainers
class PostRepositoryWithDynamicPropertiesTest {

  private static final String COUCHBASE_IMAGE_NAME = "couchbase:community";
  private static final String DEFAULT_IMAGE_NAME = "couchbase/server";
  private static final DockerImageName DEFAULT_IMAGE =
      DockerImageName.parse(COUCHBASE_IMAGE_NAME).asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

  @Container
  static final CouchbaseContainer couchbaseContainer =
      new CouchbaseContainer(DEFAULT_IMAGE)
          .withBucket(new BucketDefinition("demo").withPrimaryIndex(true))
          .withStartupTimeout(Duration.ofMinutes(10));

  @DynamicPropertySource
  static void bindCouchbaseProperties(DynamicPropertyRegistry registry) {
    registry.add("couchbase.host", couchbaseContainer::getConnectionString);
    registry.add("couchbase.adminUser", couchbaseContainer::getUsername);
    registry.add("couchbase.adminPassword", couchbaseContainer::getPassword);
    registry.add("couchbase.bucket", () -> "demo");
  }

  @Autowired private PostRepository posts;

  @Test
  void testLoadUsers() throws InterruptedException {
    // Sleeping to let DataInitilizer insert Data
    TimeUnit.SECONDS.sleep(15);
    this.posts
        .findAll(Sort.by(Sort.Direction.ASC, "title"))
        .log()
        .as(StepVerifier::create)
        .expectNextCount(2)
        .verifyComplete();
  }
}
