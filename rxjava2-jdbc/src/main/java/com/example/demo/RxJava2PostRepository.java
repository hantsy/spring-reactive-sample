/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.stereotype.Component;


/**
 *
 * @author hantsy
 */
@Component
class RxJava2PostRepository {
    private Database db;

    RxJava2PostRepository(Database db) {
        this.db = db;
    }

    Observable<Post> findAll() {
        return this.db.select("select * from posts")
            .get(
                rs -> new Post(rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("content")
                )
            )
            .toObservable();
    }

    Single<Post> findById(Long id) {
        return this.db.select("select * from posts where id=?")
            .parameter(id)
            .get(
                rs -> new Post(rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("content")
                )
            )
            .firstElement()
            .toSingle();
    }

    Single<Integer> save(Post post) {
        return this.db.update("insert into posts(title, content) values(?, ?)")
            .parameters(post.getTitle(), post.getContent())
            .returnGeneratedKeys()
            .getAs(Integer.class)
            .firstElement()
            .toSingle();
    }

}
