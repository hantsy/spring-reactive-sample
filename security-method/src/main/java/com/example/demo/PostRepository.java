/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@Component
class PostRepository {

    private static final Map<Long, Post> DATA = new HashMap<>();
    private static Long ID_COUNTER = 1L;

    static {
        Stream.of("post one", "post two").forEach(title -> {
            Long id = PostRepository.nextId();
            DATA.put(id, Post.builder().id(id).title(title).content("content of " + title).build());
        });
    }

    private static Long nextId() {
        return ID_COUNTER++;
    }

    Flux<Post> findAll() {
        return Flux.fromIterable(DATA.values());
    }

    Mono<Post> findById(Long id) {
        return Mono.just(DATA.get(id));
    }

    Mono<Post> save(Post post) {
        Long id = nextId();
        Post saved = Post.builder().id(id).title(post.getTitle()).content(post.getContent()).build();
        DATA.put(id, saved);
        return Mono.just(saved);
    }

    Mono<Post> update(Long id, Post post) {
        Post updated = DATA.get(id);
        updated.setTitle(post.getTitle());
        updated.setContent(post.getContent());
        DATA.put(id, updated);
        return Mono.just(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    Mono<Post> delete(Long id) {
        Post deleted = DATA.get(id);
        DATA.remove(id);
        return Mono.just(deleted);
    }

}
