/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hantsy
 */
@RestController
@RequestMapping(value = "/posts")
class RxJava2PostController {
    
    private final RxJava2PostRepository posts;

    public RxJava2PostController(RxJava2PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Observable<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/{id}")
    public Single<Post> get(@PathVariable(value = "id") Long id) {
        return this.posts.findById(id);
    }
    
    @PostMapping(value = "")
    public Single<Integer> create(Post post) {
        return this.posts.save(post);
    }
    
}
