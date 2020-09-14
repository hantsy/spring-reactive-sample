package com.example.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Collections;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .pathMatchers(HttpMethod.GET, "/posts").permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> routes(FavoriteHandler favoriteHandler) {
        RouterFunction<ServerResponse> usersRoutes = route(GET("/{username}/favorites"), favoriteHandler::favoritedPosts);
        RouterFunction<ServerResponse> postsRoutes = route(GET("/{slug}/favorited"), favoriteHandler::favorited)
                .andRoute(GET("/{slug}/favorites"), favoriteHandler::all)
                .andRoute(POST("/{slug}/favorites"), favoriteHandler::favorite)
                .andRoute(DELETE("/{slug}/favorites"), favoriteHandler::unfavorite);

        return nest(path("/posts"), postsRoutes)
                .andNest(path("/users"), usersRoutes);
    }
}

@Component
class FavoriteHandler {

    private ReactiveRedisConnection conn;

    public Mono<ServerResponse> favorited(ServerRequest req) {

        String slug = req.pathVariable("slug");
        return req.principal()
                .map(p -> p.getName())
                .flatMap(
                        name -> this.conn.zSetCommands()
                                .zRange(
                                        ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()),
                                        Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(-1L))
                                )
                                .map(this::toString)
                                .collectList()
                                .map(f -> Collections.singletonMap("favorited", f.contains(name)))
                )
                .flatMap(f -> ok().body(BodyInserters.fromValue(f)));

    }

    public Mono<ServerResponse> all(ServerRequest req) {

        String slug = req.pathVariable("slug");
        return this.conn.zSetCommands()
                .zRange(
                        ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()),
                        Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(-1L))
                )
                .map(this::toString)
                .collectList()
                .flatMap(f -> ok().body(BodyInserters.fromValue(f)));
    }

    public Mono<ServerResponse> favoritedPosts(ServerRequest req) {

        return req.principal()
                .map(p -> p.getName())
                .flatMap(
                        name -> this.conn.zSetCommands()
                                .zRange(
                                        ByteBuffer.wrap(("users:" + name + ":favorites").getBytes()),
                                        Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(-1L))
                                )
                                .map(this::toString)
                                .collectList()
                )
                .flatMap(f -> ok().body(BodyInserters.fromValue(f)));
    }

    public Mono<ServerResponse> favorite(ServerRequest req) {

        String slug = req.pathVariable("slug");
        return req.principal()
                .map(p -> p.getName())
                .flatMap(
                        name -> this.conn.zSetCommands()
                                .zAdd(ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()), 1.0D, ByteBuffer.wrap(name.getBytes()))
                                .then(this.conn.zSetCommands().zAdd(ByteBuffer.wrap(("users:" + name + ":favorites").getBytes()), 1.0D, ByteBuffer.wrap(slug.getBytes())))
                )
                .flatMap(f -> ok().build());
    }


    public Mono<ServerResponse> unfavorite(ServerRequest req) {
        String slug = req.pathVariable("slug");
        return req.principal()
                .map(p -> p.getName())
                .flatMap(
                        name -> this.conn.zSetCommands()
                                .zRem(ByteBuffer.wrap(("posts:" + slug + ":favorites").getBytes()), ByteBuffer.wrap(name.getBytes()))
                                .then(this.conn.zSetCommands().zRem(ByteBuffer.wrap(("users:" + name + ":favorites").getBytes()), ByteBuffer.wrap(slug.getBytes())))
                )
                .flatMap(f -> noContent().build());

    }

    private String toString(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes);
    }
}

