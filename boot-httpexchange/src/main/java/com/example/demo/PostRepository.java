package com.example.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PostRepository {
    Mono<Post> findById(UUID id);

    Flux<Post> findAll();

    Mono<UUID> save(Post post);

    Mono<Void> update(UUID id, Post post);

    Mono<Void> deleteById(UUID id);
}