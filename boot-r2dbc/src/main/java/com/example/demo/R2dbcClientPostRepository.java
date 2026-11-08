package com.example.demo;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Component
@Slf4j
public class R2dbcClientPostRepository implements PostRepository {

    public static final BiFunction<Row, RowMetadata, Post> MAPPING_FUNCTION = (row, rowMetaData) -> new Post(
            row.get("id", UUID.class),
            row.get("title", String.class),
            row.get("content", String.class),
            Status.valueOf(row.get("status", String.class)),
            row.get("created_at", LocalDateTime.class)
    );

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Post> findByTitleContains(String name) {
        String sql = """
                SELECT * FROM posts 
                         WHERE title LIKE :title
                """;
        return this.databaseClient
                .sql(sql)
                .bind("title", "%" + name + "%")
                .map(MAPPING_FUNCTION)
                .all();
    }

    @Override
    public Flux<Post> findAll() {
        String sql = """
                SELECT * FROM posts
                """;
        return this.databaseClient
                .sql(sql)
                .filter((statement, executeFunction) -> statement.fetchSize(10).execute())
                .map(MAPPING_FUNCTION)
                .all();
    }

    // see:
    // https://stackoverflow.com/questions/64267699/spring-data-r2dbc-and-group-by
    @Override
    public Flux<Map<Object, Object>> countByStatus() {
        String sql = """
                SELECT count(*) AS cnt, status 
                FROM posts 
                GROUP BY status
                """;
        return this.databaseClient
                .sql(sql)
                .map((row, rowMetadata) -> {
                    Long cnt = row.get("cnt", Long.class);
                    Status s = Status.valueOf( row.get("status", String.class));

                    return Map.<Object, Object>of("cnt", cnt, "status", s);
                })
                .all();
    }

    @Override
    public Mono<Post> findById(UUID id) {
        String sql = """
                SELECT * FROM posts 
                         WHERE id=:id
                """;
        return this.databaseClient
                .sql(sql)
                .bind("id", id)
                .map(MAPPING_FUNCTION)
                .one();
    }

    @Override
    public Mono<UUID> save(Post p) {
        String sql = """
                INSERT INTO  posts (title, content, status) 
                VALUES (:title, :content, :status)
                """;
        return this.databaseClient.sql(sql)
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .bind("title", p.title())
                .bind("content", p.content())
                .bind("status", p.status().name())
                .fetch()
                .first()
                .map(r -> (UUID) r.get("id"));
    }

    // see: https://github.com/spring-projects/spring-data-r2dbc/issues/259
    // and
    // https://stackoverflow.com/questions/62514094/how-to-execute-multiple-inserts-in-batch-in-r2dbc
    @Override
    public Flux<UUID> saveAll(List<Post> data) {
        Assert.notEmpty(data, "saving data can be empty");
        return this.databaseClient.inConnectionMany(connection -> {

            String sql = """
                    INSERT INTO posts (title, content, status) 
                    VALUES ($1, $2, $3)
                    """;
            var statement = connection
                    .createStatement(sql)
                    .returnGeneratedValues("id");

            for (int i = 0; i < data.size() - 1; i++) {
                Post p = data.get(i);
                statement.bind(0, p.title())
                        .bind(1, p.content())
                        .bind(2, p.status().name())
                        .add();
            }

            // for the last item, do not call `add`
            var lastItem = data.get(data.size() - 1);
            statement.bind(0, lastItem.title())
                    .bind(1, lastItem.content())
                    .bind(2, lastItem.status().name());

            return Flux.from(statement.execute())
                    .flatMap(result -> result.map((row, rowMetadata) -> row.get("id", UUID.class)));
        });
    }

    @Override
    public Mono<Long> update(Post p) {
        String sql = """
                UPDATE posts 
                SET title=:title,
                    content=:content,
                    status=:status 
                WHERE id=:id
                """;
        return this.databaseClient
                .sql(sql)
                .bind("title", p.title())
                .bind("content", p.content())
                .bind("status", p.status().name())
                .bind("id", p.id())
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Long> deleteById(UUID id) {
        String sql = """
                DELETE FROM posts 
                       WHERE id=:id
                """;
        return this.databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Long> deleteAllById(List<UUID> ids) {
        String sql = """
                DELETE FROM posts 
                       WHERE id in (:ids)
                """;
        return this.databaseClient.sql(sql)
                .bind("ids", ids)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Long> deleteAll() {
        String sql = """
                DELETE FROM posts
                """;
        return this.databaseClient.sql(sql)
                .fetch()
                .rowsUpdated();
    }
}
