package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.neo4j.config.EnableReactiveNeo4jAuditing;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.notFound;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // see: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.4.0-M2-Release-Notes#neo4j-1
    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    public ReactiveTransactionManager reactiveTransactionManager(
            Driver driver,
            ReactiveDatabaseSelectionProvider databaseNameProvider) {
        return new ReactiveNeo4jTransactionManager(driver, databaseNameProvider);
    }
}

@Configuration(proxyBeanMethods = false)
@EnableReactiveNeo4jAuditing
class DataConfig {

    @Bean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> Mono.just("hantsy");
    }
}

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("default")
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
                .log("[Initializing data]")
                // .blockLast(Duration.ofSeconds(5));
                //  using subscribe here will cause IntegrationTests failed.
                .subscribe(
                        data -> log.info("found post: {}", data),
                        err -> log.error("error", err),
                        () -> log.info("done")
                );

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
    @Query("MATCH(post:Post) WHERE post.title =~ $title RETURN post")
    Flux<Post> findByTitleContains(String title);

    Flux<Post> findByTitleLike(String title);
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

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
