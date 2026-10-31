
# Kotlin DSLs

Spring provides Kotlin DSLs for both halves of a WebFlux application — bean definitions and functional routes:

| DSL | Function | Handlers | Samples |
| :--- | :--- | :--- | :--- |
| Beans DSL (bean definition) | `beans { }` | Reactor types (`Mono`/`Flux`) | [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl), [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) |
| Beans DSL (bean definition) | `beans { }` | Coroutines (`suspend`/`Flow`) | [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) |
| Router DSL | `router { }` (RouterFunctionDsl) | Reactor types | [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) |
| Co router DSL | `coRouter { }` (CoRouterFunctionDsl) | Coroutines | [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) |

## The beans DSL

The Kotlin `BeanDefinitionDSL` (`org.springframework.context.support.beans`) replaces annotation configuration with functional registration.

```kotlin
fun beans() = beans {
    bean {
        CommandLineRunner {
            println("start data initialization...")
            val posts = ref<PostRepository>()
            posts.deleteAll()
                .thenMany<Post>(
                    posts.saveAll(
                        arrayListOf(
                            Post(null, "my first post", "content of my first post"),
                            Post(null, "my second post", "content of my second post")
                        )
                    )
                )
                .log()
                .subscribe { println("data initialization done.") }
        }
    }

    bean {
        PostRoutes(PostHandler(ref())).routes()
    }

    bean {
        val config = CorsConfiguration().apply { /* ... */ }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        CorsWebFilter(source)
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args) {
        addInitializers(beans())
    }
}
```

Reading the DSL:

- `bean { ... }` registers a bean with a supplier; `ref<T>()` resolves another bean by type.
- The DSL registers any bean the application needs — a `CommandLineRunner` for data initialization, the `RouterFunction`, even a `CorsWebFilter`.
- With Spring Boot, apply it with `addInitializers(beans())`; in a plain Spring application, register it on a `GenericApplicationContext` 

Check the [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl) sample shows that variant with `RouterFunctions.toWebHandler(...)`. And the [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) sample shows the Spring Boot variant.

### The router DSL

The `router { }` function (`RouterFunctionDsl`) declares functional routes with a nesting style that reads top-down, replacing the `andRoute` chain from [Annotation-Driven Kotlin and RouterFunctions](kotlin.md#routerfunctions-in-kotlin):

```kotlin
class PostRoutes(private val postHandler: PostHandler) {
    fun routes() = router {
        "/posts".nest {
            GET("", postHandler::all)
            GET("/{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("/{id}", postHandler::update)
            DELETE("/{id}", postHandler::delete)
        }
    }
}
```

The handlers here are ordinary `Mono<ServerResponse>` methods — the same shape as the [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) sample.

## The coroutine variant: coRouter and suspend handlers

The following example demonstrates the use of `coRouter { }` (`CoRouterFunctionDsl`) and coroutine handlers backed by a `CoroutineCrudRepository`:

```kotlin
class PostRoutes(private val postHandler: PostHandler) {
    fun routes() = coRouter {
        "/posts".nest {
            GET("", postHandler::all)
            GET("/{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("/{id}", postHandler::update)
            DELETE("/{id}", postHandler::delete)
        }
    }
}

class PostHandler(private val posts: PostRepository) {

    suspend fun all(req: ServerRequest): ServerResponse {
        val data = this.posts.findAll()
        return ok().bodyAndAwait(data)
    }

    suspend fun create(req: ServerRequest): ServerResponse {
        val formData = req.awaitBody<Post>()
        val saved = this.posts.save(formData)
        return created(URI.create("/posts/" + saved.id)).buildAndAwait()
    }

    suspend fun get(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")
        val post = this.posts.findById(id) ?: throw PostNotFoundException(id)
        return ok().bodyValueAndAwait(post)
    }

    suspend fun update(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")
        val existed = this.posts.findById(id) ?: throw PostNotFoundException(id)
        val formData = req.awaitBody<Post>()

        existed.apply {
            title = formData.title
            content = formData.content
        }

        this.posts.save(existed)
        return noContent().buildAndAwait()
    }

    suspend fun delete(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")
        this.posts.deleteById(id)
        return noContent().buildAndAwait()
    }
}

interface PostRepository : CoroutineCrudRepository<Post, String>,
    CoroutineSortingRepository<Post, String>
```

The suspend handlers use the coroutine request/response extensions: `awaitBody`, `bodyAndAwait`, `bodyValueAndAwait`, and `buildAndAwait`. The data initialization in the beans DSL runs inside `runBlocking { ... }`:

```kotlin
bean {
    CommandLineRunner {
        val posts = ref<PostRepository>()
        runBlocking {
            posts.deleteAll()
            posts.saveAll(arrayListOf(Post(null, "my first post", "content of my first post")))
                .collect { println("saved post: $it") }
        }
    }
}
```

Check the [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) sample.

### ExceptionFilter: a CoWebFilter for error mapping

The same sample registers a `CoWebFilter` that maps a domain exception thrown by the downstream chain to a 404 response — see [Coroutines](coroutines.md#cowebfilter) for the `CoWebFilter` contract:

```kotlin
@Component
class ExceptionFilter : CoWebFilter() {
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        try {
            chain.filter(exchange)
        } catch (e: PostNotFoundException) {
            exchange.response.statusCode = HttpStatus.NOT_FOUND
            exchange.response.setComplete().awaitSingleOrNull()
        }
    }
}
```

## BeanRegistrarDsl in Spring Framework 7

Spring Framework 7 evolves the bean registration API into `BeanRegistrarDsl` — a registrar class you `@Import` instead of an initializer. 

```kotlin
class MyBeanRegistrar : BeanRegistrarDsl({
    registerBean { InMemoryPostRepository() }
    registerBean { PostHandler(bean()) }
    registerBean { Routes(bean()) }

    registerBean(
        name = "webHandler",
        backgroundInit = true,
        prototype = false,
        supplier = {
            RouterFunctions.toWebHandler(bean<Routes>().routes(), HandlerStrategies.builder().build())
        }
    )
})

@Configuration
@Import(MyBeanRegistrar::class)
class CustomConfig
```

`registerBean(name = ..., prototype = ..., backgroundInit = ...)` controls the bean metadata explicitly, and `profile("foo") { ... }` scopes registrations to a profile. The `beans { }` DSL above remains fully supported; prefer `BeanRegistrarDsl` for new Spring Framework 7 code.

Check the [bean-registrar-kotlin-dsl](https://github.com/hantsy/spring7-sandbox/tree/main/bean-registrar-kotlin-dsl) sample of the spring7-sandbox repository for a complete example.

