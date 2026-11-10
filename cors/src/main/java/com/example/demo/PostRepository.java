package com.example.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class PostRepository {

    private final Map<Long, Post> data = new ConcurrentHashMap<>();
    private final AtomicLong nextIdGenerator = new AtomicLong(1L);

    public PostRepository() {
        Stream.of("post one", "post two").forEach(title -> {
            var id = this.nextIdGenerator.getAndIncrement();
            this.data.put(id, Post.builder().id(id).title(title).content("content of " + title).build());
        });
    }

    Flux<Post> findAll() {
        return Flux.fromIterable(this.data.values());
    }

    Mono<Post> findById(Long id) {
        return Mono.justOrEmpty(this.data.get(id));
    }

    Mono<Post> save(Post post) {
        var id = this.nextIdGenerator.getAndIncrement();
        var saved = Post.builder().id(id).title(post.getTitle()).content(post.getContent()).build();
        this.data.put(id, saved);
        return Mono.just(saved);
    }

}