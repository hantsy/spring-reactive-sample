package com.example.demo;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public ReactiveRedisTemplate<String, Post> reactiveJsonPostRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory) {

        RedisSerializationContext<String, Post> serializationContext = RedisSerializationContext
            .<String, Post>newSerializationContext(new StringRedisSerializer())
            .hashKey(new StringRedisSerializer())
            .hashValue(new Jackson2JsonRedisSerializer<>(Post.class))
            .build();


        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postController) {
        return route(GET("/posts"), postController::all)
            .andRoute(POST("/posts"), postController::create)
            .andRoute(GET("/posts/{id}"), postController::get)
            .andRoute(PUT("/posts/{id}"), postController::update)
            .andRoute(DELETE("/posts/{id}"), postController::delete);
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
        log.info("start data initialization  ...");
        this.posts
            .deleteAll()
            .thenMany(
                Flux
                    .just("Post one", "Post two")
                    .flatMap(
                        title -> {
                            String id = UUID.randomUUID().toString();
                            return this.posts.save(Post.builder().id(id).title(title).content("content of " + title).build());
                        }
                    )
            )
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done initialization...")
            );

    }

}

@Component
class PostHandler {

    private final PostRepository posts;

    public PostHandler(PostRepository posts) {
        this.posts = posts;
    }

    public Mono<ServerResponse> all(ServerRequest req) {
        return ServerResponse.ok().body(this.posts.findAll(), Post.class);
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
            .flatMap(post -> this.posts.save(post))
            .flatMap(p -> ServerResponse.created(URI.create("/posts/" + p.getId())).build());
    }

    public Mono<ServerResponse> get(ServerRequest req) {
        return this.posts.findById(req.pathVariable("id"))
            .flatMap(post -> ServerResponse.ok().body(Mono.just(post), Post.class))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest req) {

        return Mono
            .zip(
                (data) -> {
                    Post p = (Post) data[0];
                    Post p2 = (Post) data[1];
                    p.setTitle(p2.getTitle());
                    p.setContent(p2.getContent());
                    return p;
                },
                this.posts.findById(req.pathVariable("id")),
                req.bodyToMono(Post.class)
            )
            .cast(Post.class)
            .flatMap(post -> this.posts.save(post))
            .flatMap(post -> ServerResponse.noContent().build());

    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent().build(this.posts.deleteById(req.pathVariable("id")));
    }

}

@Component
class PostRepository {

    ReactiveRedisOperations<String, Post> template;

    public PostRepository(ReactiveRedisOperations<String, Post> template) {
        this.template = template;
    }

    Flux<Post> findAll() {
        return template.<String, Post>opsForHash().values("posts");
    }

    Mono<Post> findById(String id) {
        return template.<String, Post>opsForHash().get("posts", id);
    }

    Mono<Post> save(Post post) {
        if (post.getId() != null) {
            String id = UUID.randomUUID().toString();
            post.setId(id);
        }
        return template.<String, Post>opsForHash().put("posts", post.getId(), post)
            .log()
            .map(p -> post);

    }

    Mono<Void> deleteById(String id) {
        return template.<String, Post>opsForHash().remove("posts", id)
            .flatMap(p -> Mono.<Void>empty());
    }

    Mono<Boolean> deleteAll() {
        return template.<String, Post>opsForHash().delete("posts");
    }

}

//@RedisHash("posts")
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
    private LocalDateTime createdDate;
}
