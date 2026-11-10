package com.example.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PostRepository {

    Flux<Post> findAll();

    Mono<Post> findById(UUID id);

    Mono<UUID> save(Post post);

    Mono<Long> update(UUID id, Post post);

    Mono<Long> deleteById(UUID id);
}