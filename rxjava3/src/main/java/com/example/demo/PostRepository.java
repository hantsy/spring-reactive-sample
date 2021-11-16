/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author hantsy
 */
@Component
class PostRepository {

    private static final List<Post> DATA = new ArrayList<>();

    static {
        DATA.add(Post.builder().id(UUID.randomUUID()).title("post one").content("content of post one").build());
        DATA.add(Post.builder().id(UUID.randomUUID()).title("post two").content("content of post two").build());
    }

    Observable<Post> findAll() {
        return Observable.fromIterable(DATA);
    }

    Flowable<Post> flowable() {
        return Flowable.fromIterable(DATA);
    }

    Maybe<Post> findById(UUID id) {
        return findAll().filter(p -> p.getId().equals(id))
                .singleElement()
                .switchIfEmpty(Maybe.error(new PostNotFoundException(id)));
    }

    Single<Post> save(Post post) {
        Post saved = Post.builder().id(UUID.randomUUID()).title(post.getTitle()).content(post.getContent()).build();
        DATA.add(saved);
        return Single.just(saved);
    }

}
