
# Kotlin

Kotlin is a first-class citizen in the Spring ecosystem. Spring Framework provides dedicated Kotlin DSLs for bean definitions and functional routing, and Spring Data supports coroutine-based repositories. This page tours the three Kotlin samples in the repository: plain Kotlin with Reactor types, the bean/router DSLs, and coroutines.

All three samples are plain Spring applications using the manual wiring from the [Getting Started](../start/intro.md) chapter — no Spring Boot auto-configuration.

## Annotation-driven Kotlin

The [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin) sample mirrors the Java samples: annotation-driven controller, Reactor return types, Spring Data Mongo reactive repository.

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

Beyond the idiomatic `val` constructor injection and expression bodies, the notable Kotlin-specific dependency is `tools.jackson.module:jackson-module-kotlin` for JSON serialization of Kotlin classes, plus `io.projectreactor.kotlin:reactor-kotlin-extensions` for Reactor extensions.

## The bean definition DSL

The [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl) sample replaces annotations with `BeanRegistrarDsl` — a functional bean registration DSL:

```kotlin
fun beans() = BeanRegistrarDsl {

    registerBean {
        PropertySourcesPlaceholderConfigurer().apply {
            val resources = arrayOf(ClassPathResource("application.properties"))
            setLocations(*resources)
            setIgnoreUnresolvablePlaceholders(true)
        }
    }

    registerBean {
        PostHandler(bean())
    }

    registerBean {
        Routes(bean())
    }

    registerBean<WebHandler>("webHandler") {
        val router = bean<Routes>().router()
        val strategies = HandlerStrategies.builder().build()
        RouterFunctions.toWebHandler(router, strategies)
    }

    registerBean {
        ReactiveMongoTemplate(
            SimpleReactiveMongoDatabaseFactory(
                ConnectionString("mongodb://localhost:27017/blog")
            )
        )
    }

    profile("foo") {
        registerBean<Foo>()
    }
}
```

`bean()` references another registered bean by type, and `profile("foo") { ... }` scopes registrations to a profile. The application context is a plain `GenericApplicationContext` that registers the DSL:

```kotlin
class Application(port: Int = 8080) {

    private val httpHandler: HttpHandler
    private val server: HttpServer

    init {
        val context = GenericApplicationContext()
        context.register(beans())
        context.refresh()
        server = HttpServer.create().host("localhost").port(port)
        httpHandler = WebHttpHandlerBuilder.applicationContext(context).build()
    }

    fun startAndAwait() {
        server.handle(ReactorHttpHandlerAdapter(httpHandler)).bindNow().onDispose().block()
    }
}

fun main(args: Array<String>) {
    Application().startAndAwait()
}
```

## The router DSL

The same sample declares routes with the Kotlin `router { }` DSL instead of a `RouterFunction` bean:

```kotlin
class Routes(private val postHandler: PostHandler) {

    fun router() = router {
        accept(MediaType.TEXT_HTML).nest {
            GET("/") { ServerResponse.ok().render("index") }
        }
        "/api".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/posts", postHandler::all)
                GET("/posts/{id}", postHandler::get)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/posts", postHandler::stream)
            }
            POST("/posts", postHandler::create)
            PUT("/posts/{id}", postHandler::update)
            DELETE("/posts/{id}", postHandler::delete)
        }
        resources("/**", ClassPathResource("static/"))
    }
}
```

For the Spring Boot flavor of the DSLs, see [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) and [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) (`BeanDefinitionDSL` and `RouterFunctionDsl`/`CoRouterFunctionDsl`).

## Coroutines

The [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) sample replaces Reactor types with Kotlin coroutines: `suspend` functions for single results, `Flow` for streams.

```kotlin
@RestController
@RequestMapping(value = ["/posts"])
class PostController(val posts: PostRepository) {

    @GetMapping("")
    fun all(): ResponseEntity<Flow<Post>> = ResponseEntity.ok(this.posts.findAll())

    @PostMapping("")
    suspend fun create(@RequestBody post: Post): ResponseEntity<Void> {
        val saved = this.posts.save(post)
        return ResponseEntity.created(URI.create("/posts/${saved.id}")).build()
    }

    @GetMapping("/{id}")
    suspend fun get(@PathVariable("id") id: String): ResponseEntity<Post> {
        val existed = this.posts.findById(id) ?: throw PostNotFoundException(id)
        return ResponseEntity.ok(existed)
    }

    @ExceptionHandler(value = [PostNotFoundException::class])
    suspend fun handlePostNotFoundException(e: PostNotFoundException): ProblemDetail {
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "Post was not found")
        detail.apply {
            title = "Not Found"
            type = URI.create("http://example.com/not_found")
        }
        return detail
    }
}
```

The repository interface derives from `CoroutineCrudRepository` instead of a reactive repository:

```kotlin
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepository : CoroutineCrudRepository<Post, String>
```

The `findById` method returns a nullable `Post?` — idiomatic Kotlin replaces `Mono`/`switchIfEmpty` with `?: throw`. Add the coroutines dependencies to enable this style:

```xml
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-reactor</artifactId>
</dependency>
```

## Related samples

- [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin) — the annotation style on Spring Boot
- [boot-kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co) — coroutines on Spring Boot
- [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) — Kotlin functional bean registration
- [hantsy/spring-kotlin-coroutines-sample](https://github.com/hantsy/spring-kotlin-coroutines-sample) and [hantsy/spring-kotlin-dsl-sample](https://github.com/hantsy/spring-kotlin-dsl-sample) — dedicated repositories
