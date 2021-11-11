package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.springframework.data.config.EnableNeo4jAuditing;
import org.neo4j.springframework.data.core.ReactiveNeo4jClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.notFound;

@SpringBootApplication
@EnableNeo4jAuditing
@EnableTransactionManagement
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;

    @Override
    public void run(String[] args) {
        log.info("start data initialization...");
        this.posts.deleteAll()
                .thenMany(
                        Flux
                                .just("Post one", "Post two")
                                .flatMap(
                                        title -> this.posts.save(Post.builder().title(title).content("The content of " + title).build())
                                )
                )
                .log()
                .thenMany(
                        this.posts.findAll()
                )
                .blockLast();//to make `IntegrationTests` work.
//                .subscribe(
//                        (data) -> log.info("found post:" + data),
//                        (error) -> log.error("error:" + error),
//                        () -> log.info("done initialization...")
//                );

    }

}

@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @GetMapping("")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
    public Mono<Post> create(@RequestBody Post post) {
        return this.posts.save(post);
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return Mono.just(id)
                .flatMap(posts::findOne)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
        return this.posts.findOne(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(this.posts::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Integer> delete(@PathVariable("id") String id) {
        return this.posts.deleteById(id);
    }

}

@RestControllerAdvice
@Slf4j
class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    ResponseEntity postNotFound(PostNotFoundException ex) {
        log.debug("handling exception::" + ex);
        return notFound().build();
    }

}

class PostNotFoundException extends RuntimeException {

    PostNotFoundException(String id) {
        super("Post #" + id + " was not found");
    }
}

@Component
@RequiredArgsConstructor
class PostRepository {
    private final ReactiveNeo4jClient client;

    public Mono<Long> count() {
        return client.query("MATCH (p:Post) RETURN count(p)")
                .fetchAs(Long.class)
                .mappedBy((ts, r) -> r.get(0).asLong())
                .one();
    }

    public Flux<Post> findAll() {
        return client
                .query(
                        "MATCH (p:Post) " +
                                " RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt"
                )
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asString())
                                .title(
                                        r.get("title").asString())
                                .content(
                                        r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Mono<Post> findOne(String id) {
        return client
                .query(
                        "MATCH (p:Post)" +
                                " WHERE p.id = $id" +
                                " RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt"
                )
                .bind(id).to("id")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asString())
                                .title(
                                        r.get("title").asString())
                                .content(
                                        r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Post> save(Post post) {
        var query = "MERGE (p:Post {id: $id}) \n" +
                " ON CREATE SET p.createdAt=localdatetime(), p.title=$title, p.content=$content\n" +
                " ON MATCH SET p.updatedAt=localdatetime(), p.title=$title, p.content=$content\n" +
                " RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt";

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
                                .id(r.get("id").asString())
                                .title(
                                        r.get("title").asString())
                                .content(
                                        r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Integer> deleteAll() {
        return client.query("MATCH (m:Post) DETACH DELETE m")
                .run()
                .map(it -> it.counters().nodesDeleted());

    }

    public Mono<Integer> deleteById(String id) {
        return client
                .query(
                        "MATCH (p:Post) WHERE p.id = $id" +
                                " DETACH DELETE p"
                )
                .bind(id).to("id")
                .run()
                .map(it -> it.counters().nodesDeleted());
    }
}

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {
    private String id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
