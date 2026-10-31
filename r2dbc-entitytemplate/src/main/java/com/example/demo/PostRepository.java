package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostRepository {

    private final R2dbcEntityTemplate template;

    public Flux<Post> findByTitleContains(String name) {
        return this.template.select(Post.class)
                .matching(Query.query(where("title").like("%" + name + "%")).limit(10).offset(0))
                .all();
    }

    public Mono<Long> countByTitleContains(String name) {
        return this.template.count(Query.query(where("title").like("%" + name + "%")), Post.class);
    }

    public Flux<Post> findAll() {
        return this.template.select(Post.class).all();
    }

    public Mono<Long> count() {
        return this.template.count(Query.empty(), Post.class);
    }

    public Mono<Post> findById(UUID id) {
        return this.template.selectOne(Query.query(where("id").is(id)), Post.class);
    }

    public Mono<UUID> save(Post p) {
        return this.template.insert(Post.class)
                .using(p)
                .map(Post::id);
    }

    public Mono<Long> update(Post p) {
        /*
         * return this.template.update(Post.class)
         * .matching(Query.query(where("id").is(p.getId())))
         * .apply(Update.update("title", p.getTitle())
         * .set("content", p.getContent())
         * .set("status", p.getStatus())
         * .set("metadata", p.getMetadata()));
         */
        return this.template.update(
                Query.query(where("id").is(p.id())),
                Update.update("title", p.title())
                        .set("content", p.content())
                        .set("status", p.status())
                        .set("tags", p.tags()),
                Post.class);
    }

    public Mono<Long> deleteById(UUID id) {
        return this.template.delete(Query.query(where("id").is(id)), Post.class);
    }

    public Flux<UUID> saveAll(List<Post> data) {

        return Flux.fromIterable(data)
                .flatMap(p -> this.template
                        .insert(Post.class)
                        .using(p)
                        .map(Post::id)
                );

    }

    public Mono<Long> deleteAll() {
        return this.template.delete(Post.class).all();
    }

    public Mono<Long> deleteAllById(List<UUID> ids) {
        return this.template.delete(Post.class)
                .matching(Query.query(where("id").in(ids)))
                .all();
    }
}
