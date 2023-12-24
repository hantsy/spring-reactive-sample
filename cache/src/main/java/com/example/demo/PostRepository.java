package com.example.demo;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PostRepository extends R2dbcRepository<Post, UUID> {
    @Override
    @Cacheable(value = "posts", key = "#p0")
    Mono<Post> findById(UUID id);

    @Override
    @CachePut(value = "posts", key = "#result.id")
    Mono<Post> save(Post post);

    @Override
    @CacheEvict(value = "posts", key = "#p0")
    Mono<Void> deleteById(UUID id);

    @Override
    @CacheEvict(value = "posts", allEntries = true)
    Mono<Void> deleteAll();
}
