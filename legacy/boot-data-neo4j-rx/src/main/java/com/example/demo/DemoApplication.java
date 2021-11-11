package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.springframework.data.config.EnableNeo4jAuditing;
import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.repository.ReactiveNeo4jRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.notFound;

@SpringBootApplication
@EnableNeo4jAuditing
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
                .blockLast();// to make `IntegrationTests` work.
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
    public Mono<Post> get(@PathVariable("id") Long id) {
        return Mono.just(id)
                .flatMap(posts::findById)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") Long id, @RequestBody Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(this.posts::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") Long id) {
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

    PostNotFoundException(Long id) {
        super("Post #" + id + " was not found");
    }
}

interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {
}

@Node
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String content;

    @CreatedDate
    private LocalDateTime createdDate;
}
