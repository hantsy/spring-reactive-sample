# RouterFunctions

The [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) sample replaces the controller with a `RouterFunction` bean — the Kotlin spelling of the Java [RouterFunction](../web/func.md) style, using `HandlerFunction` references:

```kotlin
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
@EnableWebFlux
class WebConfig {

    @Bean
    fun routes(handler: PostHandler): RouterFunction<ServerResponse> {
        return route(GET("/posts"), HandlerFunction<ServerResponse>(handler::all))
                .andRoute(POST("/posts"), HandlerFunction<ServerResponse>(handler::create))
                .andRoute(GET("/posts/{id}"), HandlerFunction<ServerResponse>(handler::get))
                .andRoute(PUT("/posts/{id}"), HandlerFunction<ServerResponse>(handler::update))
                .andRoute(DELETE("/posts/{id}"), HandlerFunction<ServerResponse>(handler::delete))
    }
}
```

The handlers are `@Component` methods returning `Mono<ServerResponse>`:

```kotlin
@Component
class PostHandler(val posts: PostRepository) {

    fun all(req: ServerRequest): Mono<ServerResponse> {
        return ok().body(this.posts.findAll(), Post::class.java)
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        return req.bodyToMono(Post::class.java)
                .flatMap { post -> this.posts.save(post) }
                .flatMap { (id) -> created(URI.create("/posts/$id")).build() }
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
                .flatMap { post -> ok().body(Mono.just(post), Post::class.java) }
                .switchIfEmpty(notFound().build())
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
                .zipWith(req.bodyToMono(Post::class.java))
                .map { it.t1.copy(title = it.t2.title, content = it.t2.content) }
                .flatMap { this.posts.save(it) }
                .flatMap { noContent().build() }
    }
}
```

## What's next

- [Coroutines](coroutines.md) — replace `Mono`/`Flux` with `suspend` functions and `Flow`.
- [Kotlin DSLs](dsl.md) — declare the routes with `router { }`/`coRouter { }` and the beans with the Kotlin beans DSL instead of annotations.
