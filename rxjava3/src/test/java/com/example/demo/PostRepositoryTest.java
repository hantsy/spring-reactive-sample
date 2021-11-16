package com.example.demo;

import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

//see: https://www.infoq.com/articles/Testing-RxJava2/
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
        var testObserver = new TestObserver<Post>();
        this.posts.findAll().subscribe(testObserver);

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertThat(testObserver.values().size()).isEqualTo(2);
    }

    @Test
    void findById() {
        var testObserver = new TestObserver<Post>();
        this.posts.findById(UUID.randomUUID()).subscribe(testObserver);

        testObserver.assertError(PostNotFoundException.class);
        testObserver.assertNotComplete();
    }

    @Test
    void save() {
        var testObserver = new TestObserver<Post>();
        this.posts.save(Post.builder().title("test title").content("test content").build())
                .subscribe(testObserver);

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(p -> null != p.getId());
    }
}