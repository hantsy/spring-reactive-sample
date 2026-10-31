
# Coroutines

Spring WebFlux treats Kotlin coroutines as first-class citizens: a controller method can be a `suspend` function returning a single value, or a regular function returning `Flow<T>` for streams. Spring bridges to and from Reactor internally, so the reactive server stack stays unchanged.

The samples referenced here are [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) and [boot-kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co) in this repository, plus [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) and its [SecurityWebFilter](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) in the spring6-sandbox repository.

## Suspend methods in `@RestController`

Use `suspend` functions to erase the Reactor Mono/Flux wrapper types, allowing you to write code that looks synchronous while still being non-blocking.


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

Notes on the style:

- `suspend fun` replaces `Mono<T>` — the function suspends until the single result arrives.
- `Flow<Post>` replaces `Flux<T>` for multi-value streams.
- Nullability replaces `switchIfEmpty`: `findById` returns `Post?`, and the Elvis operator (`?:`) throws the not-found exception.
- Validation (`@Valid`, `@NotBlank`) works the same as in Java, and `@ExceptionHandler` handles both validation and domain exceptions.

The [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) sample of this repository shows the same pattern against MongoDB, with a `ProblemDetail`-based exception handler:

```kotlin
@ExceptionHandler(value = [PostNotFoundException::class])
suspend fun handlePostNotFoundException(e: PostNotFoundException): ProblemDetail {
    val detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "Post was not found")
    detail.apply {
        title = "Not Found"
        type = URI.create("http://example.com/not_found")
    }
    return detail
}
```

Check the [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) sample from the spring6-sandbox repository.

## Coroutine friendly Repository

Spring Data provides `CoroutineCrudRepository` for coroutine-based data access — add `kotlinx-coroutines-reactor` and the store's reactive driver:

```kotlin
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow

interface PostRepository : CoroutineCrudRepository<Post, Long>, CoroutineSortingRepository<Post, Long> {
    fun findByTitleContains(title: String): Flow<Post>
}
```

Derived query methods return `Flow` for collections and nullable types (or `suspend`) for single results — no reactive wrapper types anywhere.

## Transactions in coroutines

Spring's `TransactionalOperator` provides coroutine extensions — `executeAndAwait` for single results and the `transactional` extension for `Flow`:

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

The extensions come from `org.springframework.transaction.reactive` (`executeAndAwait`, `transactional`).

## CoWebFilter

`CoWebFilter` is the coroutine variant of the reactive `WebFilter` contract from [Web Programming](../web/web-filter.md). Instead of returning `Mono<Void>`, the `filter` method is a `suspend` function, and the chain exposes a `CoWebFilterChain` whose `filter` call suspends.

The following example demonstrates a simple security check using a `CoWebFilter`.

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

Declared as a `@Component`, the filter is picked up by component scanning and registered into the `WebHandler` chain — the same behavior as a Java `WebFilter`, with the method body written in sequential coroutine style. 

Check the [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) sample from the spring6-sandbox repository.

A `CoWebFilter` can also handle exceptions thrown by the downstream chain — the `ExceptionFilter` in the [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) sample maps a domain exception to a 404 response, see [Kotlin DSLs](dsl.md#exceptionfilter-a-cowebfilter-for-error-mapping).

## Testing

`kotlinx-coroutines-test` provides `runTest`, and [MockK](https://mockk.io/) provides `coEvery`/`coVerify` for suspending functions, [Spring Mockk](https://github.com/Ninja-Squad/springmockk) provides integration with Spring's testing framework, such as `@MockkBean/@SpykBean`. 

The following `PostControllerTest` demonstrates how to test a controller with a mocked repository using `runTest` and MockK:

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

The filter tests use the same tools — verify the `SecurityWebFilter` contract with `WebFluxTest` and `runTest`:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@WebFluxTest(controllers = [HelloController::class])
class WebFilterTests {

    @TestConfiguration
    @Import(value = [SecurityWebFilter::class])
    class TestConfig

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `authentication failed with no user params`() = runTest {
        client.get().uri("/hello")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `authentication passed with user params`() = runTest {
        client.get().uri("/hello?user=Hantsy")
            .exchange()
            .expectStatus().isOk
    }
}
```

The test dependencies: `kotlinx-coroutines-test`, `com.ninja-squad:springmockk`, and `io.kotest:kotest-assertions-core-jvm`.

## When to use which filter contract

| Contract | Signature | Use when |
| :--- | :--- | :--- |
| `WebFilter` | `Mono<Void> filter(ServerWebExchange, WebFilterChain)` | Java code, or Kotlin code already working with Reactor types |
| `CoWebFilter` | `suspend fun filter(ServerWebExchange, CoWebFilterChain)` | Kotlin coroutines code that prefers sequential, null-safe logic |
