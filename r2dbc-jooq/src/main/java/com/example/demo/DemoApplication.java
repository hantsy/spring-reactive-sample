package com.example.demo;

import com.example.demo.jooq.tables.records.PostsTagsRecord;
import io.r2dbc.spi.ConnectionFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.*;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.example.demo.jooq.Tables.*;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication()
@EnableR2dbcAuditing
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Configuration
class JooqConfig {

    @Bean
    DSLContext dslContext(ConnectionFactory connectionFactory) {
        return DSL.using(
                new TransactionAwareConnectionFactoryProxy(connectionFactory),
                SQLDialect.POSTGRES
        );
    }
}


@Configuration
@RequiredArgsConstructor
@Slf4j
class DataInitializer {
    private final DSLContext dslContext;

    @EventListener(classes = ApplicationReadyEvent.class)
    public void init() {
        Mono.from(dslContext.insertInto(POSTS)
                        .columns(POSTS.TITLE, POSTS.CONTENT)
                        .values("jooq test", "content of Jooq test")
                        .returningResult(POSTS.ID)
                )
                .flatMapMany(id -> dslContext.insertInto(COMMENTS)
                        .columns(COMMENTS.POST_ID, COMMENTS.CONTENT)
                        .values(id.component1(), "test comments")
                        .values(id.component1(), "test comments 2")
                )
                .flatMap(it -> dslContext.select(POSTS.TITLE,
                                        POSTS.CONTENT,
                                        multiset(select(COMMENTS.CONTENT)
                                                .from(COMMENTS)
                                                .where(COMMENTS.POST_ID.eq(POSTS.ID))
                                        ).as("comments")
                                )
                                .from(POSTS)
                                .orderBy(POSTS.CREATED_AT)

                )
                .subscribe(
                        data -> log.debug("saving data: {}", data.formatJSON()),
                        error -> log.debug("error: " + error),
                        () -> log.debug("done")
                );
    }
}


@Configuration
class WebConfig {

    @Bean
    RouterFunction<ServerResponse> routerFunction(PostHandler handler) {
        return route()
                .GET("/posts", handler::getAll)
                .POST("/posts", handler::create)
                .build();
    }
}


@Component
@RequiredArgsConstructor
class PostHandler {
    private final PostService postService;

    public Mono<ServerResponse> getAll(ServerRequest req) {
        return ok().body(this.postService.findAll(), PostSummary.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(CreatePostCommand.class)
                .flatMap(this.postService::create)
                .flatMap(id -> created(URI.create("/posts/" + id)).build());
    }

}


@Service
@RequiredArgsConstructor
@Slf4j
class PostService {
    private final DSLContext dslContext;

    public Flux<PostSummary> findAll() {
        var p = POSTS;
        var pt = POSTS_TAGS;
        var t = HASH_TAGS;
        var c = COMMENTS;
        var sql = dslContext.select(
                        p.ID,
                        p.TITLE,
                        DSL.field("count(comments.id)", SQLDataType.BIGINT),
                        multiset(select(t.NAME)
                                .from(t)
                                .join(pt).on(t.ID.eq(pt.TAG_ID))
                                .where(pt.POST_ID.eq(p.ID))
                        ).as("tags")
                )
                .from(p.leftJoin(c).on(c.POST_ID.eq(p.ID)))
                .groupBy(p.ID)
                .orderBy(p.CREATED_AT);
        return Flux.from(sql)
                .map(r -> new PostSummary(r.value1(), r.value2(), r.value3(), r.value4().map(Record1::value1)));
    }

    public Mono<PaginatedResult> findByKeyword(String keyword, int offset, int limit) {
        var p = POSTS;
        var pt = POSTS_TAGS;
        var t = HASH_TAGS;
        var c = COMMENTS;

        Condition where = DSL.trueCondition();
        if (StringUtils.hasText(keyword)) {
            where = where.and(p.TITLE.likeIgnoreCase("%" + keyword + "%"));
        }
        var dataSql = dslContext.select(
                        p.ID,
                        p.TITLE,
                        DSL.field("count(comments.id)", SQLDataType.BIGINT),
                        multiset(select(t.NAME)
                                .from(t)
                                .join(pt).on(t.ID.eq(pt.TAG_ID))
                                .where(pt.POST_ID.eq(p.ID))
                        ).as("tags")
                )
                .from(p.leftJoin(c).on(c.POST_ID.eq(p.ID)))
                .where(where)
                .groupBy(p.ID)
                .orderBy(p.CREATED_AT)
                .limit(offset, limit);

        val countSql = dslContext.select(DSL.field("count(*)", SQLDataType.BIGINT))
                .from(p)
                .where(where);

        return Mono
                .zip(
                        Flux.from(dataSql)
                                .map(r -> new PostSummary(r.value1(), r.value2(), r.value3(), r.value4().map(Record1::value1)))
                                .collectList(),
                        Mono.from(countSql)
                                .map(Record1::value1)
                )
                .map(it -> new PaginatedResult(it.getT1(), it.getT2()));
    }

    public Mono<UUID> create(CreatePostCommand data) {
        var p = POSTS;
        var pt = POSTS_TAGS;
        var sqlInsertPost = dslContext.insertInto(p)
                .columns(p.TITLE, p.CONTENT)
                .values(data.title(), data.content())
                .returningResult(p.ID);
        return Mono.from(sqlInsertPost)
                .flatMap(id -> {
                            Collection<?> tags = data.tagId().stream().map(tag -> {
                                PostsTagsRecord r = pt.newRecord();
                                r.setPostId(id.value1());
                                r.setTagId(tag);
                                return r;
                            }).toList();
                            return Mono.from(dslContext.insertInto(pt)
                                            .columns(pt.POST_ID, pt.TAG_ID)
                                            .values(tags)
                                    )
                                    .map(r -> {
                                        log.debug("inserted tags:: {}", r);
                                        return id;
                                    });
                        }
                )
                .map(Record1::value1);
    }
}

record CreatePostCommand(String title, String content, List<UUID> tagId) {
}

record PostSummary(UUID id, String title, Long countOfComments, List<String> tags) {
}

record PaginatedResult(List<?> data, Long count) {
}

record TagDto(UUID id, String name) {
}

interface PostRepository extends R2dbcRepository<Post, UUID> {

    @Query("SELECT * FROM posts where title like :title")
    public Flux<Post> findByTitleContains(String title);
}

interface CommentRepository extends R2dbcRepository<Comment, UUID> {
}

interface HashTagRepository extends R2dbcRepository<HashTag, UUID> {
}

interface PostTagRelationRepository extends R2dbcRepository<PostTagRelation, UUID> {
}

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "posts")
class Post {

    @Id
    @Column("id")
    private UUID id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("status")
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("created_by")
    @CreatedBy
    private String createdBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("version")
    @Version
    private Long version;
}

enum Status {
    DRAFT, PENDING_MODERATION, PUBLISHED;
}

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "comments")
class Comment {

    @Id
    @Column("id")
    private UUID id;

    @Column("content")
    private String content;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("post_id")
    private UUID postId;
}

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "hash_tags")
class HashTag {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;
}

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "posts_tags")
class PostTagRelation {

    @Column("post_id")
    private UUID postId;

    @Column("tag_id")
    private UUID tagId;
}