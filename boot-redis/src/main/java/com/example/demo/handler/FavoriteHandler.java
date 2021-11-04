package com.example.demo.handler;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collections;

import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class FavoriteHandler {

  private final ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

  FavoriteHandler(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
    this.reactiveRedisConnectionFactory = reactiveRedisConnectionFactory;
  }

  public Mono<ServerResponse> favorited(ServerRequest req) {

    String slug = req.pathVariable("slug");
    return req.principal()
        .map(Principal::getName)
        .flatMap(
            name ->
                this.reactiveRedisConnectionFactory
                    .getReactiveConnection()
                    .zSetCommands()
                    .zRange(
                        ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()),
                        Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(-1L)))
                    .map(this::toString)
                    .collectList()
                    .map(f -> Collections.singletonMap("favorited", f.contains(name))))
        .flatMap(f -> ok().body(BodyInserters.fromValue(f)));
  }

  public Mono<ServerResponse> all(ServerRequest req) {

    String slug = req.pathVariable("slug");
    return this.reactiveRedisConnectionFactory
        .getReactiveConnection()
        .zSetCommands()
        .zRange(
            ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()),
            Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(-1L)))
        .map(this::toString)
        .collectList()
        .flatMap(f -> ok().body(BodyInserters.fromValue(f)));
  }

  public Mono<ServerResponse> favoritedPosts(ServerRequest req) {

    return req.principal()
        .map(Principal::getName)
        .flatMap(
            name ->
                this.reactiveRedisConnectionFactory
                    .getReactiveConnection()
                    .zSetCommands()
                    .zRange(
                        ByteBuffer.wrap(("users:" + name + ":favorites").getBytes()),
                        Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(-1L)))
                    .map(this::toString)
                    .collectList())
        .flatMap(f -> ok().body(BodyInserters.fromValue(f)));
  }

  public Mono<ServerResponse> favorite(ServerRequest req) {

    String slug = req.pathVariable("slug");
    return req.principal()
        .map(Principal::getName)
        .flatMap(
            name ->
                this.reactiveRedisConnectionFactory
                    .getReactiveConnection()
                    .zSetCommands()
                    .zAdd(
                        ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()),
                        1.0D,
                        ByteBuffer.wrap(name.getBytes()))
                    .then(
                        this.reactiveRedisConnectionFactory
                            .getReactiveConnection()
                            .zSetCommands()
                            .zAdd(
                                ByteBuffer.wrap(("users:" + name + ":favorites").getBytes()),
                                1.0D,
                                ByteBuffer.wrap(slug.getBytes()))))
        .flatMap(f -> ok().build());
  }

  public Mono<ServerResponse> unfavorite(ServerRequest req) {
    String slug = req.pathVariable("slug");
    return req.principal()
        .map(Principal::getName)
        .flatMap(
            name ->
                this.reactiveRedisConnectionFactory
                    .getReactiveConnection()
                    .zSetCommands()
                    .zRem(
                        ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()),
                        ByteBuffer.wrap(name.getBytes()))
                    .then(
                        this.reactiveRedisConnectionFactory
                            .getReactiveConnection()
                            .zSetCommands()
                            .zRem(
                                ByteBuffer.wrap(("users:" + name + ":favorites").getBytes()),
                                ByteBuffer.wrap(slug.getBytes()))))
        .flatMap(f -> noContent().build());
  }

  private String toString(ByteBuffer byteBuffer) {
    byte[] bytes = new byte[byteBuffer.remaining()];
    byteBuffer.get(bytes);
    return new String(bytes);
  }
}
