/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.nio.ByteBuffer;
import java.util.List;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
@RestController()
@RequestMapping(value = "/favorites")
class FavoriteController {

    private final ReactiveRedisConnectionFactory factory;

    public FavoriteController(ReactiveRedisConnectionFactory factory) {
        this.factory = factory;
    }

    @GetMapping("")
    public Mono<List<String>> all() {
        return this.factory.getReactiveConnection()
                .setCommands()
                .sMembers(ByteBuffer.wrap("users:user:favorites".getBytes()))
                .map(FavoriteController::toString)
                .collectList();
    }

    private static String toString(ByteBuffer byteBuffer) {

        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

}
