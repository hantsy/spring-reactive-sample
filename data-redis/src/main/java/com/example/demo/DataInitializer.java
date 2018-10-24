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
class DataInitializer {

    private final ReactiveRedisConnectionFactory factory;
    private final PostRepository posts;
    private final RedisSerializer<String> serializer = new StringRedisSerializer();

    public DataInitializer(ReactiveRedisConnectionFactory factory, PostRepository posts) {
        this.factory = factory;
        this.posts = posts;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.initPosts();

        ReactiveRedisConnection conn = this.factory.getReactiveConnection();
        log.info("print all keys  ...");
        conn.keyCommands() //
            .keys(ByteBuffer.wrap(serializer.serialize("*"))) //
            .flatMapMany(Flux::fromIterable) //
            .doOnNext(byteBuffer -> System.out.println(toString(byteBuffer))) //
            .count() //
            .doOnSuccess(count -> System.out.println(String.format("Total No. found: %s", count))) //
            .block();

        conn.setCommands()
            .sAdd(
                ByteBuffer.wrap("users:user:favorites".getBytes()),
                this.posts.findAll()
                    .map(p -> ByteBuffer.wrap(p.getId().getBytes()))
                    .collectList().block()
            )
            //.log()
            .doOnSuccess(s -> log.info("added favirates...#" + s))
            .subscribe();

        log.info("print ramdon keys  ...");
        ReactiveKeyCommands keyCommands = conn.keyCommands();
        keyCommands.randomKey()
            .doOnNext(byteBuffer -> System.out.println(toString(byteBuffer))) //
            .flatMap(keyCommands::type)
            .doOnSuccess(type -> System.out.println(String.format("ByteBuffer type: %s", type))) //
            .block();

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
