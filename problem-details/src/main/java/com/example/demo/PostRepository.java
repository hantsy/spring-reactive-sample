package com.example.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface PostRepository {

    Flux<Post> findByTitleContains(String title);

    Flux<Post> findAll();

    Mono<Post> findById(UUID id);

    Mono<UUID> save(Post post);

    Flux<UUID> saveAll(List<Post> data);

    Mono<Long> update(Post post);

    Mono<Long> deleteById(UUID id);

    Mono<Long> deleteAll();
}
