
# Coroutines

Spring WebFlux supports Kotlin coroutines as a first-class programming model. In practice, this means you can write controller methods as `suspend` functions that return a single value, or as regular functions returning `Flow<T>` when the API produces a stream of values.

At the framework boundary, Spring bridges between coroutine-based code and Reactor primitives (`Mono` and `Flux`) internally, so the reactive runtime remains the same while the application code becomes more idiomatic and easier to read.

This document focuses on the coroutine patterns used in the sample projects in this repository:

- [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co)
- [boot-kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co)

## `suspend` controller methods

Use `suspend` functions to remove Reactor wrapper types from the method signature. The method still executes asynchronously and non-blockingly, but the body reads like ordinary Kotlin code.

```kotlin
@RestController
@RequestMapping("/")
@Validated
class PostController(private val postRepository: PostRepository) {

    @GetMapping("")
    fun findAll(): Flow<Post> = postRepository.findAll()

    @GetMapping("{id}")
    suspend fun findOne(@PathVariable id: Long): Post? =
        postRepository.findById(id) ?: throw PostNotFoundException(id)

    @GetMapping("byTitle")
    fun findByName(@RequestParam @NotBlank title: String): Flow<Post> =
        postRepository.findByTitleContains(title)

    @PostMapping("")
    suspend fun save(@RequestBody @Valid body: CreatePostCommand): Post =
        postRepository.save(Post(title = body.title, content = body.content))

    @ExceptionHandler(value = [PostNotFoundException::class])
    fun handleNotFound(ex: Exception, exchange: ServerWebExchange): ResponseEntity<Any> {
        return status(NOT_FOUND).body(ex.message)
    }
}
```

Comparison with a Reactor-based controller shows the differences:

- `suspend fun` replaces `Mono<T>`. The function suspends until the result becomes available.
- `Flow<T>` replaces `Flux<T>` for streaming collections.
- `Post?` replaces `switchIfEmpty(...)` patterns. When a value is missing, the Elvis operator (`?:`) can raise a domain exception.
- `@Valid`, `@NotBlank`, and other validation annotations behave exactly as in a Java-based Spring MVC or WebFlux controller.
- `@ExceptionHandler` remains the normal mechanism for translating domain exceptions into HTTP responses.


## Coroutine-friendly repository interfaces

Spring Data provides `CoroutineCrudRepository` for coroutine-based persistence operations. In typical usage, you add the coroutine support dependency and the reactive driver for your datastore.

```kotlin
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface PostRepository : CoroutineCrudRepository<Post, Long>, CoroutineSortingRepository<Post, Long> {
    fun findByTitleContains(title: String): Flow<Post>
}
```

Derived repository methods naturally return:

- `Flow<T>` for collection-valued queries
- nullable `T?` or `suspend` single-result methods for one item
- no Reactor wrapper types in the repository API itself

This keeps the repository layer consistent with the rest of the coroutine-based application code.

## Transactions with coroutines

Spring provides coroutine-aware transaction extensions via `TransactionalOperator`. The most common patterns are:

- `executeAndAwait` for single-value transaction results
- `transactional(...)` for `Flow` pipelines

```kotlin
// single-result transaction
val post = txOperator.executeAndAwait { tx: ReactiveTransaction ->
    posts.save(Post(title = "foobar", content = "content of foobar"))
}

// transactional Flow pipeline
flowOf("test 1", "test 2", "test 3")
    .onEach { posts.save(Post(title = it, content = "content of $it")) }
    .transactional(txOperator, newSingleThreadContext("MyContext"))
    .collect { log.debug("saved post: $it") }
```

These extensions come from the `org.springframework.transaction.reactive` package and integrate with the reactive transaction infrastructure while allowing coroutine-style sequencing.

## `CoWebFilter`

`CoWebFilter` is the coroutine version of the reactive `WebFilter` contract described in the Web Programming chapter. Instead of returning `Mono<Void>`, the filter method is `suspend` and the chain exposes a `CoWebFilterChain`, whose `filter` call also suspends.

### Security filter example

```kotlin
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange

@Component
class SecurityWebFilter : CoWebFilter() {
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        if (!exchange.request.queryParams.containsKey("user")) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        }
        return chain.filter(exchange)
    }
}
```

When declared as a `@Component`, the filter is discovered by component scanning and registered into the `WebHandler` chain. The behavior matches a Java `WebFilter`, but the logic is expressed in a straightforward sequential coroutine style.

The [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) sample uses `CoWebFilter` in a real application — its exception filter is shown below.

### Exception filter example

A `CoWebFilter` can also catch exceptions thrown by downstream handlers or filters. The [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) sample registers an `ExceptionFilter` that maps a domain exception to an HTTP 404 response:

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

See [Kotlin DSLs](dsl.md) for the broader sample usage.

## Testing coroutine-based WebFlux applications

For coroutine-aware testing, use the Kotlin coroutines test utilities together with your preferred mocking library. A common setup uses:

- [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test)
- [MockK](https://mockk.io/)
- [Spring MockK](https://github.com/Ninja-Squad/springmockk) for Spring integration

Additionally, [kotest-assertions-jvm](https://github.com/kotest/kotest/tree/master/kotest-assertions/kotest-assertions-jvm) provides coroutine-friendly assertions for testing suspending functions.

### Example: controller test with `runTest` and MockK

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@WebFluxTest(controllers = [PostController::class])
class PostControllerTest {

    @MockkBean
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `get posts by title with invalid title`() = runTest {
        coEvery { postRepository.findByTitleContains(any()) } returns
            flowOf(Post(id = 1, title = "test title", content = "test content"))

        client.get().uri("/byTitle?title=")
            .exchange()
            .expectStatus().isBadRequest

        coVerify(exactly = 0) { postRepository.findByTitleContains(any()) }
    }

    @Test
    fun `get post that not found`() = runTest {
        val id = 1L
        coEvery { postRepository.findById(any()) } throws PostNotFoundException(id)

        client.get().uri("/$id")
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { postRepository.findById(any()) }
    }
}
```

### Example: suspend-based test method

Recent JUnit-based Kotlin testing supports suspending test methods directly, which allows a `@Test` function to be declared as `suspend fun` without wrapping the body in `runTest`. This is especially convenient when the test code calls other suspending collaborators.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@WebFluxTest(controllers = [PostController::class])
class PostControllerTests {

    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var posts: PostRepository

    @Test
    suspend fun `get all posts`() {
        coEvery { posts.findAll() } returns flowOf(
            Post(id = "1", title = "post one", content = "content of post one"),
            Post(id = "2", title = "post two", content = "content of post two")
        )

        client.get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.size()").isEqualTo(2)

        coVerify(exactly = 1) { posts.findAll() }
    }
}
```

## Summary

Kotlin coroutines fit naturally into Spring WebFlux without abandoning the reactive foundation. The common rule of thumb is:

- use `suspend` for single-value operations
- use `Flow` for streaming collections
- keep validation and exception handling in the usual Spring style
- use coroutine-aware repository and transaction APIs for clean control flow

This produces code that is easier to read and maintain while preserving the performance and scalability characteristics of reactive programming.

