
# Kotlin DSLs

Spring Framework 7 ships two Kotlin DSLs that replace annotation-driven configuration and the `RouterFunction` builder chain: `BeanRegistrarDsl` for bean definitions and `coRouter { }` for coroutine-friendly functional routes.

## BeanRegistrarDsl

`BeanRegistrarDsl` is the Spring Framework 7 bean definition DSL (the successor of the older `BeanDefinitionDsl`/`beans { }`). From the [bean-registrar-kotlin-dsl](https://github.com/hantsy/spring7-sandbox/tree/main/bean-registrar-kotlin-dsl) sample of the spring7-sandbox repository:

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
    register(FoobarRegistrar())
})

class FoobarRegistrar : BeanRegistrarDsl({
    profile("foo") {
        registerBean<Bar> { Bar(bean<Foo>()) }
        registerBean { Foo() }
    }
})

class Bar(foo: Foo)
class Foo
```

Reading the DSL:

- `registerBean { ... }` registers a bean with a supplier; `bean()` resolves another bean by type.
- `registerBean(name = ..., prototype = ..., backgroundInit = ...)` controls the bean metadata explicitly.
- `register(...)` composes other registrars, and `profile("foo") { ... }` scopes registrations to a profile.

The registrar is imported into a `@Configuration` class:

```kotlin
@Configuration
@Import(MyBeanRegistrar::class)
class CustomConfig
```

And the application bootstraps without Spring Boot — the manual wiring from [Getting Started](../start/first.md#bootstrap), in Kotlin:

```kotlin
fun main(args: Array<String>) {
    val context = AnnotationConfigApplicationContext()
    context.register(CustomConfig::class.java)
    context.refresh()
    nettyServer(context).onDispose().block()
}

fun nettyServer(context: ApplicationContext): DisposableServer {
    val handler = WebHttpHandlerBuilder.applicationContext(context).build()
    val adapter = ReactorHttpHandlerAdapter(handler)
    val httpServer = HttpServer.create().host("localhost").port(8080)
    return httpServer.handle(adapter).bindNow()
}
```

The [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl) sample of this repository shows the same DSL wired against MongoDB, including registering a `WebHandler` via `RouterFunctions.toWebHandler(...)` and a message source bean.

## coRouter: the router DSL

The `coRouter { }` DSL declares functional routes for coroutine handlers. From the same sandbox sample:

```kotlin
import org.springframework.web.reactive.function.server.coRouter

class Routes(val postHandler: PostHandler) {
    fun routes() = coRouter {
        "/posts".nest {
            "{id}".nest {
                GET(accept(APPLICATION_JSON), postHandler::get)
                PUT(contentType(APPLICATION_JSON), postHandler::update)
                DELETE(accept(APPLICATION_JSON), postHandler::delete)
            }
            GET(accept(APPLICATION_JSON), postHandler::all)
            POST(contentType(APPLICATION_JSON), postHandler::create)
        }
    }
}
```

`"/posts".nest { ... }` groups the routes under a path prefix and `"{id}".nest { ... }` captures the path variable — a nesting style that reads top-down instead of the `andRoute` chain in Java.

## Suspend handlers

The handlers behind `coRouter` are `suspend` functions on `ServerRequest`, with `awaitBody`, `bodyAndAwait`, and the `buildAndAwait`/`bodyValueAndAwait` response extensions:

```kotlin
class PostHandler(val posts: PostRepository) {

    suspend fun all(req: ServerRequest): ServerResponse {
        return ServerResponse.ok().bodyAndAwait(this.posts.findAll())
    }

    suspend fun create(req: ServerRequest): ServerResponse {
        val body = req.awaitBody(Post::class)
        val saved = this.posts.save(body)
        return ServerResponse.created(URI.create("/posts/$saved")).buildAndAwait()
    }

    suspend fun get(req: ServerRequest): ServerResponse {
        val id = UUID.fromString(req.pathVariable("id"))
        val found = this.posts.findById(id)
        return found?.let { ServerResponse.ok().bodyValueAndAwait(found) }
            ?: ServerResponse.notFound().buildAndAwait()
    }

    suspend fun delete(req: ServerRequest): ServerResponse {
        val id = UUID.fromString(req.pathVariable("id"))
        return if (this.posts.deleteById(id) > 0) {
            ServerResponse.noContent().buildAndAwait()
        } else {
            ServerResponse.notFound().buildAndAwait()
        }
    }
}
```

The repository interface here is a plain Kotlin interface with `suspend` functions and `Flow` — no Spring Data required:

```kotlin
interface PostRepository {
    suspend fun findById(id: UUID): Post?
    fun findAll(): Flow<Post>
    suspend fun save(post: Post): UUID
    suspend fun update(id: UUID, post: Post): Long
    suspend fun deleteById(id: UUID): Long
}
```

## Related samples

- [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) and [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) — the Boot variants with `BeanDefinitionDSL` and `RouterFunctionDsl`/`CoRouterFunctionDsl`
- [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) — Kotlin functional bean registration
- [hantsy/spring-kotlin-dsl-sample](https://github.com/hantsy/spring-kotlin-dsl-sample) — a dedicated repository for the DSLs
