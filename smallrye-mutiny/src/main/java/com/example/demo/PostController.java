package com.example.demo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * @author hantsy
 */
@RestController
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Multi<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/{id}")
    public Uni<Post> get(@PathVariable(value = "id") UUID id) {
        return this.posts.findById(id);
    }

    @PostMapping(value = "")
    public Uni<ResponseEntity<?>> create(@RequestBody Post post) {
        return this.posts.save(post).map(p -> ResponseEntity.created(URI.create("/posts/" + p.getId())).build());
    }

}
