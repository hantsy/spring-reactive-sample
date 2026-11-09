package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @MessageMapping("posts.findAll")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @MessageMapping("posts.titleContains")
    public Flux<Post> titleContains(@Payload String title) {
        return this.posts.findByTitleContains(title);
    }

    @MessageMapping("posts.findById.{id}")
    public Mono<Post> get(@DestinationVariable("id") UUID id) {
        return this.posts.findById(id);
    }

    @MessageMapping("posts.save")
    public Mono<UUID> create(@Payload Post post) {
        return this.posts.save(post);
    }

    @MessageMapping("posts.update.{id}")
    public Mono<Boolean> update(@DestinationVariable("id") UUID id, @Payload Post post) {
        return this.posts.findById(id)
                .map(p -> new Post(p.id(), post.title(), post.content(), post.status(), p.createdAt()))
                .flatMap(this.posts::update)
                .map(updated -> updated > 0);
    }

    @MessageMapping("posts.deleteById.{id}")
    public Mono<Boolean> delete(@DestinationVariable("id") UUID id) {
        return this.posts.deleteById(id)
                .map(deleted -> deleted > 0);
    }
}