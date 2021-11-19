package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Configuration
@EnableReactiveMongoAuditing
class DataConfig{

    @Bean
    ReactiveAuditorAware<String> auditorAware() {
        return () -> Mono.just("hantsy");
    }
}

@Component
@Slf4j
@Profile("default")
class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String[] args) {
        log.info("start data initialization ...");
        this.posts
                .deleteAll()
                .thenMany(
                        Flux
                                .just("Post one", "Post two")
                                .flatMap(
                                        title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
                                )
                )
                .thenMany(
                        this.posts.findAll()
                )
                .subscribe(
                        data -> log.info("found posts: {}", posts),
                        error -> log.error("" + error),
                        () -> log.info("done initialization...")
                );

    }

}

@RestController()
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

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
        return this.posts.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(this.posts::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return this.posts.deleteById(id);
    }

}

interface PostRepository extends ReactiveMongoRepository<Post, String> {

    @Query(
            value = """
                    {
                         "title" : {
                             "$regularExpression" : { "pattern" : ?0, "options" : ""}
                         }
                    }
                    """,
            sort = """
                    { 
                        "title" : 1 , 
                        "createdDate" : -1
                    } 
                    """
    )
    Flux<Post> findByKeyword(String q);

    Flux<PostSummary> findByTitleContains(String title);

    Flux<PostSummary> findByTitleContains(String title, Pageable page);
}

@Document(collection = "posts")
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {


    @Id
    private String id;
    private String title;
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Version
    Long version;
}

@Data
class PostSummary {
    private String title;
}

