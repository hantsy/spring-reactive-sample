/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author hantsy
 */
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {

    private final PostRepository posts;


    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.initPosts();

        log.info("done data initialization  ...");
        //conn.hashCommands().hGetAll(key)
    }

    private void initPosts() {
        this.posts.deleteAll()
            .thenMany(
                Flux.just("Post one", "Post two")
                    .flatMap(title -> this.posts.save(Post.builder().id(UUID.randomUUID().toString()).title(title).content("content of " + title).build()))
            );
            //.subscribe(null, null, () -> log.info("done posts initialization..."));
    }

    private static String toString(ByteBuffer byteBuffer) {

        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

}
