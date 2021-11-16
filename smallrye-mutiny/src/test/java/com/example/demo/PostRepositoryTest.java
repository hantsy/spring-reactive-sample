package com.example.demo;

import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(Application.class)
class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAll() {
        var subscriber = this.posts.findAll()
                .subscribe()
                .withSubscriber(AssertSubscriber.create(2));
        var items = subscriber.assertCompleted().getItems();
        assertThat(items.size()).isEqualTo(2);
    }

    @Test
    void findById() {
        var subscriber = this.posts.findById(UUID.randomUUID())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailedWith(PostNotFoundException.class);
    }

    @Test
    void save() {
        var subscriber = this.posts.save(Post.builder().title("test post").content("test content").build())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());
        var item = subscriber.assertCompleted().getItem();
        assertThat(item.getId()).isNotNull();
    }
}