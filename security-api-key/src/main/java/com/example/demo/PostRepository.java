package com.example.demo;

import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
class PostRepository {

    private final List<Post> posts = List.of(
        new Post("post one", "content of post one"),
        new Post("post two", "content of post two"));

    Flux<Post> findAll() {
        return Flux.fromIterable(this.posts);
    }

}
