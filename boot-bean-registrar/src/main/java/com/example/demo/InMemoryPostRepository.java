package com.example.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryPostRepository implements PostRepository {

    private final Map<UUID, Post> posts = new HashMap<>();

    @Override
    public Flux<Post> findAll() {
        return Flux.fromIterable(posts.values());
    }

    @Override
    public Mono<Post> findById(UUID id) {
        return Mono.justOrEmpty(posts.get(id));
    }

    @Override
    public Mono<UUID> save(Post post) {
        UUID id = UUID.randomUUID();
        Post newPost = new Post(id, post.title(), post.content());
        posts.put(id, newPost);
        return Mono.just(id);
    }

    @Override
    public Mono<Long> deleteById(UUID id) {
        posts.remove(id);
        return Mono.just(1L);
    }

    @Override
    public Mono<Long> update(UUID id, Post post) {
        if (posts.containsKey(id)) {
            Post updatedPost = new Post(id, post.title(), post.content());
            posts.put(id, updatedPost);
            return Mono.just(1L);
        } else {
            return Mono.just(0L);
        }
    }
}