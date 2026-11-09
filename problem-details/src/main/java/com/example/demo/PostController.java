package com.example.demo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@RequestMapping("/posts")
@Validated
@RequiredArgsConstructor
public class PostController {

    private final PostRepository posts;

    @GetMapping
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@RequestBody @Valid CreatePostCommand data) {
        return this.posts.save(Post.of(data.title(), data.content()))
                .map(id -> created(URI.create("/posts/" + id)).build());
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Post>> get(@PathVariable UUID id) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .map(ResponseEntity::ok);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable UUID id, @RequestBody @Valid UpdatePostCommand data) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .flatMap(post -> this.posts.update(
                        new Post(post.id(), data.title(), data.content(), post.status(), post.createdAt())
                ))
                .map(__ -> noContent().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable UUID id) {
        return this.posts.deleteById(id)
                .mapNotNull(deleted -> {
                    if (deleted > 0) {
                        return noContent().build();
                    }
                    throw new PostNotFoundException(id);
                });
    }
}
