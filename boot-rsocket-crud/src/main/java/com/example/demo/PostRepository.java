package com.example.demo;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

interface PostRepository extends R2dbcRepository<Post, Integer> {

    Flux<Post> findByTitleContains(String name);
}
