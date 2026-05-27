package com.example.demo.domain;

import com.example.demo.CacheConfig;
import com.example.demo.ContainersConfiguration;
import com.example.demo.DataR2dbcConfig;
import com.example.demo.Post;
import com.example.demo.PostRepository;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(initializers = {ContainersConfiguration.class})
@SpringJUnitConfig(classes = { PostRepositoryTest.TestConfig.class })
public class PostRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(PostRepositoryTest.class);

    @Autowired
    PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var counter = new CountDownLatch(1);
        this.posts.deleteAll()
                .doOnTerminate(counter::countDown)
                .subscribe(v -> log.debug("deleted all posts"));
        counter.await(500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSaveAll() {
        var data = Post.of("test", "content");
        var post = posts.save(data).block();

        log.debug("Calling findById at the first time.");
        var firstCalled = posts.findById(post.id()).block();
        log.debug("The first call of saved post:{}", firstCalled);

        log.debug("Calling findById at the second time.");
        var secondCalled = posts.findById(post.id()).block();
        log.debug("The second call of saved post:{}", secondCalled);

        log.debug("deleting post by id...");
        posts.deleteById(post.id()).block();
        log.debug("after deleteById.");

        log.debug("call findAll which is not cached.");
        posts.findAll().subscribe(p -> log.debug("get saved post: {}", p));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Import(value = {
            DataR2dbcConfig.class,
            CacheConfig.class
    })
    static class TestConfig {
    }
}
