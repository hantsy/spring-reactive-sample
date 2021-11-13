package com.example.demo.repository.template;

import com.example.demo.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.ReactiveNeo4jOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.neo4j.cypherdsl.core.Cypher.*;

@Component
@RequiredArgsConstructor
public class TemplatePostRepository  {
    private final ReactiveNeo4jOperations template;


    public Mono<Long> count() {
        return this.template.count(Post.class);
    }


    public Flux<Post> findAll() {
        return this.template.findAll(Post.class);
    }


    public Mono<Post> findById(Long id) {
        return this.template.findById(id, Post.class);
    }


    public Flux<Post> findByTitleContains(String title) {
        var postNode = node("Post").named("p");
        return this.template.findAll(
                match(postNode)
                        .where(postNode.property("title").contains(literalOf(title)))
                        .returning(postNode)
                        .build(),
                Post.class
        );
    }

    public Mono<Post> save(Post post) {
        return this.template.save(post);
    }


    public Flux<Post> saveAll(List<Post> data) {
        return this.template.saveAll(data);
    }


    public Mono<Void> deleteById(Long id) {
        return this.template.deleteById(id, Post.class);
    }


    public Mono<Void> deleteAll() {
        return this.template.deleteAll(Post.class);
    }
}
