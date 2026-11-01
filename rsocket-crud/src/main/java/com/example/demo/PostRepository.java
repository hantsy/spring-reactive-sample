package com.example.demo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

interface PostRepository extends R2dbcRepository<Post, Integer> {

    @Query("SELECT * FROM posts WHERE title like $1")
    Flux<Post> findByTitleContains(String name);
}
