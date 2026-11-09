package com.example.demo;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPostRepository implements PostRepository {

    private final Map<UUID, Post> posts = new ConcurrentHashMap<>();

    @Override
    public Flux<Post> findAll() {
        return Flux.fromIterable(posts.values());
    }

    @Override
    public Mono<Post> findById(UUID id) {
        if (posts.containsKey(id)) {
            return Mono.just(posts.get(id));
        }

        return Mono.error(new PostNotFoundException(id));
    }

    @Override
    public Mono<UUID> save(Post post) {
        UUID id = UUID.randomUUID();
        Post newPost = new Post(id, post.title(), post.content(), Status.DRAFT, LocalDateTime.now());
        posts.put(id, newPost);
        return Mono.just(id);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        if (posts.containsKey(id)) {
            posts.remove(id);
            return Mono.empty();
        }
        return Mono.error(new PostNotFoundException(id));
    }

    @Override
    public Mono<Void> update(UUID id, Post post) {
        if (posts.containsKey(id)) {
            Post updatedPost = new Post(id, post.title(), post.content(), post.status(), posts.get(id).createdAt());
            posts.put(id, updatedPost);
            return Mono.empty();
        }

        return Mono.error(new PostNotFoundException(id));
    }
}