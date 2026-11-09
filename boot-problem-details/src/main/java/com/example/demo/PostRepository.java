package com.example.demo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PostRepository extends ReactiveCrudRepository<Post, UUID> {

    Flux<Post> findByTitleContains(String title);
}
