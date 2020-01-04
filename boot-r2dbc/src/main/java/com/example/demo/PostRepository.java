package com.example.demo;


import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.r2dbc.query.Criteria.where;

@RequiredArgsConstructor
@Component
public class PostRepository {

    private final DatabaseClient databaseClient;

    Flux<Post> findByTitleContains(String name) {
        return this.databaseClient.select()
                .from(Post.class)
                .matching(where("title").like(name))
                .fetch()
                .all();
    }

    public Flux<Post> findAll() {
        return this.databaseClient.select()
                .from(Post.class)
                .fetch()
                .all();
    }

    public Mono<Post> findById(Integer id) {
        return this.databaseClient.select()
                .from(Post.class)
                .matching(where("id").is(id))
                .fetch()
                .one();
    }

    public Mono<Integer> save(Post p) {
        return this.databaseClient.insert().into(Post.class)
                .using(p)
                .fetch()
                .one()
                .map(m -> (Integer) m.get("id"));
    }

    public Mono<Integer> update(Post p) {
        return this.databaseClient.update()
                .table(Post.class)
                .using(p)
                .fetch()
                .rowsUpdated();
    }

    public Mono<Integer> deleteById(Integer id) {
        return this.databaseClient.delete().from(Post.class)
                .matching(where("id").is(id))
                .fetch()
                .rowsUpdated();
    }
}