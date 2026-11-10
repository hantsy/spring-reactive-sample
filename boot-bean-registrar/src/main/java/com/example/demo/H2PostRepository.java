package com.example.demo;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.BiFunction;

public class H2PostRepository implements PostRepository {

    private static final BiFunction<Row, RowMetadata, Post> POST_MAPPER = (row, metadata) -> new Post(
            row.get("id", UUID.class),
            row.get("title", String.class),
            row.get("content", String.class)
    );

    private final DatabaseClient databaseClient;

    public H2PostRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Post> findById(UUID id) {
        String sql = """
                SELECT * FROM posts
                WHERE id = :id
                """;
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(POST_MAPPER)
                .one();
    }

    @Override
    public Flux<Post> findAll() {
        String sql = """
                SELECT * FROM posts
                """;
        return databaseClient.sql(sql)
                .map(POST_MAPPER)
                .all();
    }

    @Override
    public Mono<UUID> save(Post post) {
        String sql = """
                SELECT id FROM FINAL TABLE(
                    INSERT INTO posts(title, content)
                    VALUES (:title, :content)
                )
                """;
        return databaseClient.sql(sql)
                .bind("title", post.title())
                .bind("content", post.content())
                .fetch()
                .first()
                .map(row -> (UUID) row.get("id"));
    }

    @Override
    public Mono<Long> update(UUID id, Post post) {
        String sql = """
                UPDATE posts
                SET title = :title, content = :content
                WHERE id = :id
                """;
        return databaseClient.sql(sql)
                .bind("title", post.title())
                .bind("content", post.content())
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Long> deleteById(UUID id) {
        String sql = """
                DELETE FROM posts
                WHERE id = :id
                """;
        return databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }
}