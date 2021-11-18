/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rx.Observable;
import rx.Single;

/**
 *
 * @author hantsy
 */
@RestController
@RequestMapping(value = "/posts")
class RxJavaPostController {
    
    private final RxJavaPostRepository posts;

    public RxJavaPostController(RxJavaPostRepository posts) {
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
