package com.example.demo.repository.client;

import com.example.demo.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientPostRepository {

    private final ReactiveNeo4jClient client;

    public Mono<Long> count() {
        var query = """
                MATCH (p:Post) RETURN count(p)
                """;
        return client.query(query)
                .fetchAs(Long.class)
                .mappedBy((ts, r) -> r.get(0).asLong())
                .one();
    }

    public Flux<Post> findAll() {
        var query = """
                MATCH (p:Post)
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Flux<Post> findByTitleContains(String title) {
        var query = """
                MATCH (p:Post)
                WHERE p.title =~ $title
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .bind("(?!).*" + title + ".*").to("title")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Mono<Post> findById(Long id) {
        var query = """
                MATCH (p:Post)
                WHERE p.id = $id
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .bind(id).to("id")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Post> save(Post post) {
        var query = """
                MERGE (p:Post {id: $id})
                ON CREATE SET p.createdAt=localdatetime(), p.title=$title, p.content=$content
                ON MATCH SET p.updatedAt=localdatetime(), p.title=$title, p.content=$content
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;

        return client.query(query)
                .bind(post).with(data ->
                        Map.of(
                                "id", (data.getId() != null ? data.getId() : UUID.randomUUID().toString()),
                                "title", data.getTitle(),
                                "content", data.getContent()
                        )
                )
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Integer> deleteAll() {
        var query = """
                MATCH (m:Post) DETACH DELETE m
                """;
        return client.query(query)
                .run()
                .map(it -> it.counters().nodesDeleted());

    }

    public Mono<Integer> deleteById(Long id) {
        var query = """
                MATCH (p:Post) WHERE p.id = $id
                DETACH DELETE p
                """;
        return client
                .query(query)
                .bind(id).to("id")
                .run()
                .map(it -> it.counters().nodesDeleted());
    }
}
