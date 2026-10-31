
# Annotation-Driven Kotlin and RouterFunctions

This page covers the Kotlin equivalents of the two WebFlux programming models from [Getting Started](../start/intro.md) and [RouterFunction](../web/func.md): annotated `@RestController`s and `RouterFunctions`-based functional routes — both keeping the Reactor types (`Mono`, `Flux`). For the coroutine style, see [Coroutines](coroutines.md); for the Kotlin DSLs, see [Kotlin DSLs](dsl.md).

The samples referenced here are [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin), [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin), and [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes).

## Setting up a Kotlin WebFlux project

The essential dependencies (versions are managed by the Spring Boot parent):

```xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-reactor</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor.kotlin</groupId>
    <artifactId>reactor-kotlin-extensions</artifactId>
</dependency>
<dependency>
    <groupId>tools.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
</dependency>
```

The Kotlin compiler needs the *allopen* plugin so Spring classes and `@Component`-annotated classes can be proxied:

```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <configuration>
        <args>
            <arg>-Xjsr305=strict</arg>
        </args>
        <compilerPlugins>
            <plugin>spring</plugin>
        </compilerPlugins>
        <jvmTarget>21</jvmTarget>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-allopen</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

`-Xjsr305=strict` makes Spring's nullability annotations strict, so the Kotlin compiler enforces null-safety at the Spring API boundaries.

## Annotated controller

The [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin) sample mirrors the Java samples — annotation-driven controller, Reactor return types, Spring Data Mongo reactive repository:

```kotlin
@RestController
@RequestMapping(value = ["/posts"])
class PostController(val posts: PostRepository) {

    @GetMapping("")
    fun all(): Flux<Post> = this.posts.findAll()

    @PostMapping("")
    fun create(@RequestBody post: Post): Mono<Post> = this.posts.save(post)

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: String): Mono<Post> = this.posts.findById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: String, @RequestBody post: Post): Mono<Post> {
        return this.posts.findById(id)
                .map { it.copy(title = post.title, content = post.content) }
                .flatMap { this.posts.save(it) }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String): Mono<Void> = this.posts.deleteById(id)
}
```

The idiomatic Kotlin details: `val` constructor injection, expression bodies, and `it.copy(...)` on the immutable data class instead of setters.

## A Spring Boot variant

The [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin) sample uses the same controller with Spring Boot auto-configuration — the application class is minimal:

```kotlin
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    SpringApplication.run(DemoApplication::class.java, *args)
}
```

## RouterFunctions in Kotlin

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
