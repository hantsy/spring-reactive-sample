/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Single;

/**
 *
 * @author hantsy
 */
@Component
class RxJavaPostRepository {

    private static final List<Post> DATA = new ArrayList<>();

    static {
        DATA.add(Post.builder().id(1L).title("post one").content("content of post one").build());
        DATA.add(Post.builder().id(2L).title("post two").content("content of post two").build());
    }

    Observable<Post> findAll() {
        return Observable.from(DATA);
    }

    Single<Post> findById(Long id) {
        return findAll().filter(p -> Objects.equals(p.getId(), id)).single().toSingle();
    }

    Single<Post> save(Post post) {
        long id = DATA.size() + 1;
        Post saved = Post.builder().id(id).title(post.getTitle()).content(post.getContent()).build();
        DATA.add(saved);
        return Single.just(saved);
    }

}
