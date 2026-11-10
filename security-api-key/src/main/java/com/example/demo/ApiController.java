package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
class ApiController {

    private final PostRepository posts;

    ApiController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("/posts")
    Flux<Post> posts() {
        return this.posts.findAll();
    }

}
