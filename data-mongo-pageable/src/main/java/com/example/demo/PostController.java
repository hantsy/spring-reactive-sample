/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @GetMapping("/search")
    public Flux<Post> search(
        @RequestParam() String q,
        @RequestParam(name="page",  defaultValue = "0") Integer page,
        @RequestParam(name="size",  defaultValue = "10") Integer size
    ) {
        return this.posts.findByTitleLike(q, PageRequest.of(page, size));
    }

}