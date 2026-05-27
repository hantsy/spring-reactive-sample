package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postController) {
        return route(GET("/posts"), postController::all)
                .andRoute(POST("/posts"), postController::create)
                .andRoute(GET("/posts/{id}"), postController::get)
                .andRoute(PUT("/posts/{id}"), postController::update)
                .andRoute(DELETE("/posts/{id}"), postController::delete);
    }
}

@Component
class PostHandler {

    private final PostRepository posts;

    public PostHandler(PostRepository posts) {
        this.posts = posts;
    }

    public Mono<ServerResponse> all(ServerRequest req) {
        return ServerResponse.ok().body(this.posts.findAll(), Post.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
                .flatMap(post -> this.posts.save(post))
                .flatMap(p -> ServerResponse.created(URI.create("/posts/" + p.getId())).build());
    }

    public Mono<ServerResponse> get(ServerRequest req) {
        return this.posts.findById(UUID.fromString(req.pathVariable("id")))
                .flatMap(post -> ServerResponse.ok().body(Mono.just(post), Post.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return req.bodyToMono(Post.class)
                .flatMap(post -> this.posts.update(id, post))
                .flatMap(updated -> ServerResponse.ok().body(Mono.just(updated), Post.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return this.posts.deleteById(id)
                .flatMap(deleted -> deleted
                        ? ServerResponse.noContent().build()
                        : ServerResponse.notFound().build());
    }

}

@Component
@Slf4j
class PostRepository {

    private static final List<Post> DATA = new CopyOnWriteArrayList<>();

    static {
        DATA.add(Post.builder().id(UUID.randomUUID()).title("post one").content("content of post one").build());
        DATA.add(Post.builder().id(UUID.randomUUID()).title("post two").content("content of post two").build());
    }

    Flux<Post> findAll() {
        return Flux.fromIterable(DATA);
    }

    Mono<Post> findById(UUID id) {
        return findAll().filter(p -> p.getId().equals(id)).last();
    }

    Mono<Post> save(Post post) {
        Post saved = Post.builder().id(UUID.randomUUID()).title(post.getTitle()).content(post.getContent()).build();
        log.debug("saved post: {}", saved);
        DATA.add(saved);
        return Mono.just(saved);
    }

    Mono<Post> update(UUID id, Post post) {
        return Mono.justOrEmpty(DATA.stream().filter(p -> p.getId().equals(id)).findFirst())
                .flatMap(existing -> {
                    Post updated = Post.builder()
                            .id(id)
                            .title(post.getTitle())
                            .content(post.getContent())
                            .build();
                    int index = DATA.indexOf(existing);
                    if (index >= 0) {
                        DATA.set(index, updated);
                    }
                    return Mono.just(updated);
                });
    }

    Mono<Boolean> deleteById(UUID id) {
        boolean removed = DATA.removeIf(post -> post.getId().equals(id));
        return Mono.just(removed);
    }

}

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    private UUID id;
    private String title;
    private String content;
}
