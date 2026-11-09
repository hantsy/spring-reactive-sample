package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
public class PostApiController implements PostApi {
    private final PostRepository posts;

    public PostApiController(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public Flux<Post> allPosts() {
        return this.posts.findAll();
    }

    @Override
    public Mono<Post> getById(UUID id) {
        return this.posts.findById(id);
    }

    @Override
    public Mono<ResponseEntity<Void>> save(Post post) {
        return this.posts.save(post)
                .map(id -> ResponseEntity.created(URI.create("/posts/" + id)).build());
    }

    @Override
    public Mono<ResponseEntity<Void>> update(UUID id, Post post) {
        return this.posts.update(id, post)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> delete(UUID id) {
        return this.posts.deleteById(id)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}