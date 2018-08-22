/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author hantsy
 */
@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;
    private final ReactiveRedisOperations<String, Post> reactiveRedisOperations;


    @GetMapping("")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
    public Mono<Long> save(@RequestBody Post post) {
        return this.reactiveRedisOperations.convertAndSend("posts", post );
    }

    @GetMapping("{id}")
    public Mono<Post> byId(@PathVariable("id") String id) {
        return this.posts.findById(id);
    }


}
