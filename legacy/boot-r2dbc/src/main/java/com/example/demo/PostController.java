package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.ResponseEntity.*;


/**
 * @author hantsy
 */
@RestController()
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
    public Mono<ResponseEntity> create(@RequestBody Post post) {

        return this.posts.save(post)
                .map(n -> created(URI.create("/posts/" + n)).build());
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") Integer id) {
        return this.posts.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") Integer id, @RequestBody Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());
                    return p;
                })
                .flatMap(this.posts::update)
                .map(n -> n > 0 ? noContent().build() : notFound().build())
                .defaultIfEmpty(notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity> delete(@PathVariable("id") Integer id) {
        return this.posts.deleteById(id).map(n -> n > 0 ? noContent().build() : notFound().build());
    }

}