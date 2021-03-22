---
sort: 3
---

# RouterFunction

You have to know the *functional programming* is so hot in these days. Spring introduced functional style in the 5.x era.  Utilize with the new `RouterFunction`, you can handle your web requests in a series of fluent APIs instead of writing a  `@RestController`.

For example,  there is a simple controller class.

```java
@RestController
@RequestMapping
class MessageController {

    @GetMapping
    Flux<Message> allMessages(){
        return Flux.just(
            Message.builder().body("hello Spring 5").build(),
            Message.builder().body("hello Spring Boot 2").build()
        );
    }
   
}
```

You can write the same functionality with a  `RouterFunction` bean instead.

```java    
@Bean
public RouterFunction<ServerResponse> routes() {
    return route(GET("/"),(ServerRequest req)-> ok()
                 .body(
                     BodyInserters.fromObject(
                         Arrays.asList(
                             Message.builder().body("hello Spring 5").build(),
                             Message.builder().body("hello Spring Boot 2").build()
                         )
                     )
                 )
                );
}
```

The  `reoute`(from `RouterFunctions`) accepts a `RequestPredicate` and `HandlerFunction`.  `HandlerFunction` is a  `@FunctionalInteface`.

`RouterFunctions` and `RequestPredicates` are helpers to make it easy to assemble the request routes and predicates.

```java
@FunctionalInterface
public interface HandlerFunction<T extends ServerResponse> {
    Mono<T> handle(ServerRequest var1);
}
```

For the complete codes,  check [spring-reactive-sample/boot-start](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-start) and  [spring-reactive-sample/boot-start-routes](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-start-routes).

You can extract the handler codes into a new class.

The following code fragments demonstrates the same features of `PostController`  but written in `RouterFunction`.

```java
@Bean
public RouterFunction<ServerResponse> routes(PostHandler postController) {
    return route(GET("/posts"), postController::all)
        .andRoute(POST("/posts"), postController::create)
        .andRoute(GET("/posts/{id}"), postController::get)
        .andRoute(PUT("/posts/{id}"), postController::update)
        .andRoute(DELETE("/posts/{id}"), postController::delete);
}
```

The implementation of `HandlerFunction` are centralized in a  `PostHandler` class.

```java
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
```

For the complete codes,  check  [spring-reactive-sample/boot-routes](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-routes).

> NOTE: The `RouterFunction` is ported back to Servlet stack since 5.2, check my example [hantsy/spring-webmvc-functional-sample](https://github.com/hantsy/spring-webmvc-functional-sample).