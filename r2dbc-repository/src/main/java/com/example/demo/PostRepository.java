package com.example.demo;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PostRepository extends R2dbcRepository<Post, UUID> {
    public Flux<Post> findByTitleContains(String name);

    public Mono<Long> countByTitleContains(String name);

}