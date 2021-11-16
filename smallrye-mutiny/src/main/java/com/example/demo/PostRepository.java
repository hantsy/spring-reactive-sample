/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author hantsy
 */
@Component
@Slf4j
class PostRepository {

    private static final List<Post> DATA = new ArrayList<>();

    static {
        DATA.add(Post.builder().id(UUID.randomUUID()).title("post one").content("content of post one").build());
        DATA.add(Post.builder().id(UUID.randomUUID()).title("post two").content("content of post two").build());
    }

    Multi<Post> findAll() {
        return Multi.createFrom().iterable(DATA);
    }


    Uni<Post> findById(UUID id) {
        return findAll().filter(p -> p.getId().equals(id)).toUni()
                .log()
                .onItem().ifNull().failWith(new PostNotFoundException(id));
    }

    Uni<Post> save(Post post) {
        Post saved = Post.builder().id(UUID.randomUUID()).title(post.getTitle()).content(post.getContent()).build();
        log.debug("saved post: {}", saved);
        DATA.add(saved);
        return Uni.createFrom().item(saved);
    }

}
