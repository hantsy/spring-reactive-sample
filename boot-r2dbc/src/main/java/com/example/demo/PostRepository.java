package com.example.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PostRepository {
    Flux<Post> findByTitleContains(String name);

    Flux<Post> findAll();

    // see:
    // https://stackoverflow.com/questions/64267699/spring-data-r2dbc-and-group-by
    Flux<Map<Object, Object>> countByStatus();

    Mono<Post> findById(UUID id);

    Mono<UUID> save(Post p);

    // see: https://github.com/spring-projects/spring-data-r2dbc/issues/259
    // and
    // https://stackoverflow.com/questions/62514094/how-to-execute-multiple-inserts-in-batch-in-r2dbc
    Flux<UUID> saveAll(List<Post> data);

    Mono<Long> update(Post p);

    Mono<Long> deleteById(UUID id);

    Mono<Long> deleteAllById(List<UUID> ids);

    Mono<Long> deleteAll();
}
