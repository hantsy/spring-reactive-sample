
# Kotlin DSLs

Spring ships Kotlin DSLs for the main configuration surfaces of a WebFlux application: bean registration, functional routing, and security. They replace verbose builder chains and annotation ceremony with typed, nested lambda blocks that read top-down, while producing exactly the same bean definitions, `RouterFunction`s, and filter chains as the Java APIs.

This page walks through the four DSLs used in the samples:

| DSL | Purpose | Samples |
| :--- | :--- | :--- |
| `beans { }` (`BeanDefinitionDsl`) | Functional bean definitions (deprecated in 7.1) | [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl), [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl), [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) |
| `BeanRegistrarDsl` | Bean registration, Spring Framework 7+ | [boot-bean-registrar-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-bean-registrar-kotlin-dsl) |
| `router { }` / `coRouter { }` | Functional routes (Reactor / coroutines) | [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl), [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl), [boot-security-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-security-kotlin-dsl) |
| `http { }` (Security) | `SecurityWebFilterChain` configuration | [boot-security-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-security-kotlin-dsl) |

## The beans DSL (BeanDefinitionDsl)

The `beans { }` function (`org.springframework.context.support.beans`) replaces annotation configuration with functional registration. Define it as a `fun` (or `val`) and apply it as an application context initializer:

```kotlin
import org.springframework.context.support.beans

@Suppress("DEPRECATION")
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
- With Spring Boot, apply it with `addInitializers(beans())`; in a plain Spring application, register the initializer on a `GenericApplicationContext`.

**Note:** In Spring Framework 7.1+, the `beans { }` function is deprecated in favor of `BeanRegistrarDsl` (see [below](#beanregistrardsl)). The samples use `@Suppress("DEPRECATION")` to silence the warning while staying compatible with both Spring Framework 6.x and 7.x. For new code targeting Spring Framework 7+, prefer `BeanRegistrarDsl`.

The [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl) sample shows the plain-Spring variant with `RouterFunctions.toWebHandler(...)`; [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) shows the Spring Boot variant; [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) combines it with coroutine handlers.

## BeanRegistrarDsl

Spring Framework 7 evolves bean registration into the `BeanRegistrar` contract; `BeanRegistrarDsl` is its Kotlin face — a registrar class you `@Import` instead of an initializer. See [Programmatic Bean Registration with BeanRegistrar](../start/bean-registrar.md) for the Java API and the motivations behind it.

```kotlin
import org.springframework.beans.factory.BeanRegistrarDsl

class PostBeanRegistrar : BeanRegistrarDsl({
    registerBean { InMemoryPostRepository() }
    registerBean { PostHandler(bean()) }
    registerBean { Routes(bean()) }
    registerBean { bean<Routes>().routes() }
    registerBean {
        CommandLineRunner {
            runBlocking { bean<PostRepository>().initializeSampleData() }
        }
    }
    register(FoobarRegistrar())
})

class FoobarRegistrar : BeanRegistrarDsl({
    profile("foo") {
        registerBean<Bar> { Bar(bean<Foo>()) }
        registerBean { Foo() }
    }
})
```

Reading the DSL:

- `registerBean { ... }` is reified — the bean type is inferred from the supplier's return value; pass an explicit type argument (`registerBean<Bar> { ... }`) when registering against a supertype.
- `bean<T>()` inside a supplier resolves a dependency at instantiation time, the DSL equivalent of `SupplierContext.bean(...)`.
- Named parameters expose the bean metadata: `name`, `autowirable`, `backgroundInit`, `description`, `fallback`, `infrastructure`, `lazyInit`, `order`, `primary`, `prototype`.
- `profile("foo") { ... }` scopes registrations to a profile; the expression also supports `!`, `&`, and `|` combinations.
- `register(other)` composes another registrar, as `FoobarRegistrar` above.

Import the registrar on the application class — no initializer needed:

```kotlin
@SpringBootApplication
@Import(PostBeanRegistrar::class)
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
```

Check the [boot-bean-registrar-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-bean-registrar-kotlin-dsl) sample for a complete application: coroutine handlers, `coRouter` routes, and sample-data initialization, all registered through the DSL.

## Router DSLs: router and coRouter

The `router { }` function (`RouterFunctionDsl`) declares functional routes with a nesting style that reads top-down, replacing the `andRoute` chain from [RouterFunctions](router-functions.md):

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

The handlers here are ordinary `Mono<ServerResponse>` methods — the same shape as the [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) sample. Check [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) and [boot-security-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-security-kotlin-dsl) for `router { }` in a Boot application.

### The coroutine variant: coRouter and suspend handlers

`coRouter { }` (`CoRouterFunctionDsl`) is the coroutine counterpart — routes reference `suspend` handlers, and the DSL bridges them into the reactive runtime:

```kotlin
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
}
```

The suspend handlers use the coroutine request/response extensions: `awaitBody`, `bodyAndAwait`, `bodyValueAndAwait`, and `buildAndAwait`. Request predicates such as `accept(...)` and `contentType(...)` work exactly as in `router { }`.

Check the [boot-bean-registrar-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-bean-registrar-kotlin-dsl) sample for the routes above, and [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) for `coRouter` backed by a `CoroutineCrudRepository`. When the surrounding contract is blocking — a `CommandLineRunner`, for example — wrap coroutine work in `runBlocking { ... }`:

```kotlin
registerBean {
    CommandLineRunner {
        runBlocking { bean<PostRepository>().initializeSampleData() }
    }
}
```

## Security: the http DSL

Spring Security's reactive configuration has a Kotlin DSL too. Import the `invoke` operator and call `http { ... }` on the injected `ServerHttpSecurity` — the block configures the chain and the call returns the built `SecurityWebFilterChain`, so it fits directly as the expression body of a `@Bean` method:

```kotlin
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http {
            csrf { disable() }
            httpBasic {}
            authorizeExchange {
                authorize("/auth/**", authenticated)
                authorize(pathMatchers(HttpMethod.GET, "/posts/**"), permitAll)
                authorize(pathMatchers(HttpMethod.DELETE, "/posts/**"), hasRole("ADMIN"))
                authorize("/posts/**", authenticated)
                authorize(anyExchange, permitAll)
            }
        }
}
```

Reading the DSL:

- `csrf { }`, `httpBasic { }`, and `authorizeExchange { }` mirror the Java configurer methods; an empty block (`httpBasic {}`) enables the feature with defaults.
- Inside `authorizeExchange`, `authorize(path, access)` maps a pattern to an access decision — `authenticated`, `permitAll`, `hasRole("ADMIN")` — and `anyExchange` is the catch-all. Rules are evaluated in declaration order.
- `pathMatchers(HttpMethod.GET, "/posts/**")` (from `ServerWebExchangeMatchers`) restricts a rule to one HTTP method.

Check the [boot-security-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-security-kotlin-dsl) sample for the complete application: in-memory users, `router { }` endpoints, and `WebTestClient` tests covering anonymous, basic-auth, and role-restricted requests. The servlet stack has an equivalent `http { }` DSL for `HttpSecurity` (`org.springframework.security.config.annotation.web.invoke`).

