package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
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
    public Mono<Post> get(@DestinationVariable("id") Integer id) {
        return this.posts.findById(id);
    }

    @MessageMapping("posts.save")
    public Mono<Post> create(@Payload Post post) {
        return this.posts.save(post);
    }

    @MessageMapping("posts.update.{id}")
    public Mono<Post> update(@DestinationVariable("id") Integer id, @Payload Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());
                    return p;
                })
                .flatMap(p -> this.posts.save(p));
    }

    @MessageMapping("posts.deleteById.{id}")
    public Mono<Void> delete(@DestinationVariable("id") Integer id) {
        return this.posts.deleteById(id);
    }
}
