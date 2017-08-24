/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.nio.ByteBuffer;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 *
 * @author hantsy
 */
@RestController()
@RequestMapping(value = "/favorites")
class PostController {

    private final ReactiveRedisConnectionFactory factory;

    public PostController(ReactiveRedisConnectionFactory factory) {
        this.factory = factory;
    }

    @GetMapping("")
    public Flux<String> all() {
        return this.factory.getReactiveConnection()
            .setCommands()
            .sMembers(ByteBuffer.wrap("users:user:favorites".getBytes()))
            .map(PostController::toString);
    }

    private static String toString(ByteBuffer byteBuffer) {

        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes);
    }
//
//    @PostMapping("")
//    public Mono<Post> create(@RequestBody Post post) {
//        return this.posts.save(post);
//    }
//
//    @GetMapping("/{id}")
//    public Mono<Post> get(@PathVariable("id") String id) {
//        return this.posts.findById(id);
//    }
//
//    @PutMapping("/{id}")
//    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
//        return this.posts.findById(id)
//            .map(p -> {
//                p.setTitle(post.getTitle());
//                p.setContent(post.getContent());
//
//                return p;
//            })
//            .flatMap(p -> this.posts.save(p));
//    }
//
//    @DeleteMapping("/{id}")
//    public Mono<Void> delete(@PathVariable("id") String id) {
//        return this.posts.deleteById(id);
//    }

}
