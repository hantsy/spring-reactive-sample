package com.example.demo;

import java.net.URI;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
        .flatMap(this.posts::save)
        .flatMap(p -> ServerResponse.created(URI.create("/posts/" + p.getId())).build());
  }

  public Mono<ServerResponse> get(ServerRequest req) {
    return this.posts.findById(req.pathVariable("id"))
        .flatMap(post -> ServerResponse.ok().body(Mono.just(post), Post.class))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> update(ServerRequest req) {

    return Mono
        .zip(
            (data) -> {
              Post p = (Post) data[0];
              Post p2 = (Post) data[1];
              p.setTitle(p2.getTitle());
              p.setContent(p2.getContent());
              return p;
            },
            this.posts.findById(req.pathVariable("id")),
            req.bodyToMono(Post.class)
        )
        .cast(Post.class)
        .flatMap(this.posts::save)
        .flatMap(post -> ServerResponse.noContent().build());

  }

  public Mono<ServerResponse> delete(ServerRequest req) {
    return ServerResponse.noContent().build(this.posts.deleteById(req.pathVariable("id")));
  }

}
