package com.example.demo

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class PostHandler {

    PostRepository posts

    PostHandler(PostRepository posts){
        this.posts = posts
    }

    Mono<ServerResponse> all(ServerRequest req) {
        return ServerResponse.ok().body(this.posts.findAll(), Post.class)
    }

    Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
                .flatMap { this.posts.save(it) }
                .flatMap { ServerResponse.created(URI.create("/posts/".concat(it.getId()))).build() }
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.posts.findById(req.pathVariable("id"))
                .flatMap { ServerResponse.ok().body(Mono.just(it), Post.class) }
                .switchIfEmpty { ServerResponse.notFound().build() }
    }

    Mono<ServerResponse> update(ServerRequest req) {

        return Mono
                .zip(
                {
                    Post p = (Post) it[0]
                    Post p2 = (Post) it[1]
                    p.title = p2.title
                    p.content = p2.content
                    p
                },
                this.posts.findById(req.pathVariable("id")),
                req.bodyToMono(Post.class)
        )
                .cast(Post.class)
                .flatMap { this.posts.save(it) }
                .flatMap { ServerResponse.noContent().build() }

    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent().build(this.posts.deleteById(req.pathVariable("id")))
    }

}



