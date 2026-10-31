package com.example.demo;


import com.example.demo.jooq.Tables;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest()
@Slf4j
public class PostRepositoryTest {

    @TestConfiguration
    @Import({TestcontainersConfiguration.class, JooqConfig.class})
    static class TestConfig {
    }

    @Autowired
    R2dbcEntityTemplate template;

    @Autowired
    PostRepository posts;

    @Autowired
    DSLContext dslContext;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.template.delete(Comment.class).all()
                .then(this.template.delete(PostTagRelation.class).all())
                .then(this.template.delete(Post.class).all())
                .doOnTerminate(latch::countDown)
                .subscribe();
        latch.await(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testDatabaseClientExisted() {
        assertNotNull(template);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(posts);
    }

    @Test
    public void testDslContextExisted() {
        assertNotNull(dslContext);
    }

    @SneakyThrows
    @Test
    public void testInsertAndQuery() {

        var latch = new CountDownLatch(1);

        var data = Post.builder().title("test title").content("content of test").build();
        this.template.insert(data)
                .flatMapMany(
                        p -> Flux.just(1, 2, 3).map(i -> Comment.builder().postId(p.getId()).content("comment #" + i).build())
                                .flatMap(c -> this.template.insert(c))
                )
                .doOnTerminate(latch::countDown)
                .subscribe();
        latch.await(1000, TimeUnit.MILLISECONDS);

        var p = Tables.POSTS;
        var c = Tables.COMMENTS;

        var selectSql = dslContext.select(
                        p.ID,
                        p.TITLE,
                        multiset(
                                select(c.CONTENT)
                                        .from(c)
                                        .where(c.POST_ID.eq(p.ID))
                        ).as("comments")
                )
                .from(p)
                .orderBy(p.CREATED_AT.desc());
        Flux.from(selectSql)
                .as(StepVerifier::create)
                .consumeNextWith(r -> {
                            log.info("saved post: {}", r.formatJSON());
                            assertThat(r.value2()).isEqualTo("test title");
                            assertThat(r.value3().map(Record1::value1)).containsExactlyInAnyOrder("comment #1", "comment #2", "comment #3");
                        }
                )
                .verifyComplete();

    }
}
