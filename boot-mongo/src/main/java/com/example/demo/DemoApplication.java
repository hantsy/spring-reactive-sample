package com.example.demo;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.mongodb.client.result.DeleteResult;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

}


@Component
@Slf4j
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
                    title -> this.posts
                        .save(Post.builder().title(title).content("content of " + title)
                            .build())
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
    return this.posts.deleteById(id).then();
  }

}

@Component
@RequiredArgsConstructor
class PostRepository {

  private final ReactiveMongoTemplate template;

  Flux<Post> findByKeyword(String q) {
    var reg = ".*" + q + ".*";
    var criteria = where("title").regex(reg).orOperator(where("content").regex(reg));
    return template.find(query(criteria), Post.class);
  }

  Flux<Post> findByTitleContains(String title) {
    var reg = ".*" + title + ".*";
    return template.find(query(where("title").regex(reg)), Post.class);
  }

  Flux<Post> findByTitleContains(String title, Pageable page) {
    var reg = ".*" + title + ".*";
    return template
        .find(query(where("title").regex(reg)).with(page), Post.class);
  }

  public Flux<Post> findAll() {
    return template.findAll(Post.class);
  }

  public Mono<Post> save(Post post) {
    return template.save(post);
  }

  public Flux<Post> saveAll(List<Post> data) {
    return Flux.fromIterable(data).flatMap(template::save);
  }

  public Mono<Post> findById(String id) {
    return template.findById(id, Post.class);
  }

  public Mono<Long> deleteById(String id) {
    //return template.remove(Post.class).matching(query(where("id").is(id))).all().map(DeleteResult::getDeletedCount)
    return template.remove(query(where("id").is(id)), Post.class)
        .map(DeleteResult::getDeletedCount);
  }

  public Mono<Long> deleteAll() {
    return template.remove(Post.class).all().map(DeleteResult::getDeletedCount);
  }
}

@Document
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
  @Builder.Default
  private LocalDateTime createdDate = LocalDateTime.now();
}

@Data
class PostSummary {

  private String title;
}

