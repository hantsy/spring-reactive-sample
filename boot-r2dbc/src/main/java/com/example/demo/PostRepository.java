package com.example.demo;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@RequiredArgsConstructor
@Component
public class PostRepository {

  private final DatabaseClient databaseClient;

  public static final BiFunction<Row, RowMetadata, Post> MAPPING_FUNCTION =
      (row, rowMetaData) ->
          Post.builder()
              .id(row.get("id", Integer.class))
              .title(row.get("title", String.class))
              .content(row.get("content", String.class))
              .build();

  Flux<Post> findByTitleContains(String name) {
    return this.databaseClient
        .sql("SELECT * FROM posts WHERE title LIKE :title")
        .bind("title", "%" + name + "%")
        .map(MAPPING_FUNCTION)
        .all();
  }

  public Flux<Post> findAll() {
    return this.databaseClient
        .sql("SELECT * FROM posts")
        .filter((statement, executeFunction) -> statement.fetchSize(10).execute())
        .map(MAPPING_FUNCTION)
        .all();
  }

  public Mono<Post> findById(Integer id) {
    return this.databaseClient
        .sql("SELECT * FROM posts WHERE id=:id")
        .bind("id", id)
        .map(MAPPING_FUNCTION)
        .one();
  }

  public Mono<Integer> save(Post p) {
    return this.databaseClient
        .sql("INSERT INTO  posts (title, content) VALUES (:title, :content)")
        .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
        .bind("title", p.getTitle())
        .bind("content", p.getContent())
        .fetch()
        .first()
        .map(r -> (Integer) r.get("id"));
  }

  public Mono<Integer> update(Post p) {
    return this.databaseClient
        .sql("UPDATE posts set title=:title, content=:content WHERE id=:id")
        .bind("title", p.getTitle())
        .bind("content", p.getContent())
        .bind("id", p.getId())
        .fetch()
        .rowsUpdated();
  }

  public Mono<Integer> deleteById(Integer id) {
    return this.databaseClient
        .sql("DELETE FROM posts WHERE id=:id")
        .bind("id", id)
        .fetch()
        .rowsUpdated();
  }
}
