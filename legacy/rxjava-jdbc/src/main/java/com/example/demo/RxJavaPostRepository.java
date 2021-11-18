/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.davidmoten.rx.jdbc.Database;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Single;

/**
 * @author hantsy
 */
@Component
class RxJavaPostRepository {

    private Database db;

    RxJavaPostRepository(Database db) {
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
           .asObservable();
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
            .first()
            .toSingle();
    }

    Single<Integer> save(Post post) {
        return this.db.update("insert into posts(title, content) values(?, ?)")
            .parameter(post.getTitle())
            .parameter(post.getContent())
            .returnGeneratedKeys()
            .getAs(Integer.class)
            .toSingle();
    }

}
