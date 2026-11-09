package com.example.demo;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.service.RSocketExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PostClient {

    @RSocketExchange("posts.findAll")
    Flux<Post> all();

    @RSocketExchange("posts.titleContains")
    Flux<Post> titleContains(@Payload String title);

    @RSocketExchange("posts.findById.{id}")
    Mono<Post> get(@DestinationVariable("id") UUID id);

    @RSocketExchange("posts.save")
    Mono<UUID> create(@Payload Post post);

    @RSocketExchange("posts.update.{id}")
    Mono<Boolean> update(@DestinationVariable("id") UUID id, @Payload Post post);

    @RSocketExchange("posts.deleteById.{id}")
    Mono<Boolean> delete(@DestinationVariable("id") UUID id);
}