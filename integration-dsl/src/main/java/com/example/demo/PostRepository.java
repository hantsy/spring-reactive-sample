/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class PostRepository {

    private Map<Long, Post> data = new HashMap<>();
    private AtomicLong nextIdGenerator = new AtomicLong(1L);

    public PostRepository() {
        Stream.of("post one", "post two").forEach(title -> {
            Long id = this.nextId();
            data.put(id, Post.builder().id(id).title(title).content("content of " + title).build());
        });
    }

    private Long nextId() {
        return nextIdGenerator.getAndIncrement();
    }

    Flux<Post> findAll() {
        return Flux.fromIterable(data.values());
    }

    Mono<Post> findById(Long id) {
        return Mono.just(data.get(id));
    }

    Mono<Post> save(Post post) {
        Long id = nextId();
        Post saved = Post.builder().id(id).title(post.getTitle()).content(post.getContent()).build();
        data.put(id, saved);
        return Mono.just(saved);
    }

    Mono<Post> update(Long id, Post post) {
        Post updated = data.get(id);
        updated.setTitle(post.getTitle());
        updated.setContent(post.getContent());
        data.put(id, updated);
        return Mono.just(updated);
    }

    Mono<Post> delete(Long id) {
        Post deleted = data.get(id);
        data.remove(id);
        return Mono.just(deleted);
    }

}
