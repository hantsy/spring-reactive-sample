package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable Long id) {
        return this.posts.findById(id);
    }

    @PostMapping
    public Mono<Post> create(@RequestBody Post post) {
        return this.posts.save(post);
    }

}