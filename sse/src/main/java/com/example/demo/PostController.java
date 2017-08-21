/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Stream;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 *
 * @author hantsy
 */
@RestController
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Post> allStream() {
        return Flux.interval(Duration.ofSeconds(1L)).flatMap((oneSecond)->this.posts.findAll());
    }

    @GetMapping(value = "/{id}")
    public Mono<Post> get(@PathVariable(value = "id") Long id) {
        return this.posts.findById(id);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PostEvent> getEvents(@PathVariable(value = "id") Long id) {

        return this.posts.findById(id).flatMapMany(post -> {
            Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
            Flux<PostEvent> postEventFlux = Flux.fromStream(Stream.generate(() -> new PostEvent(post, new Date())));
            return Flux.zip(interval, postEventFlux).map(Tuple2::getT2);
        });
    }

    @PostMapping(value = "")
    public Mono<Post> create(Post post) {
        return this.posts.save(post);
    }

}
