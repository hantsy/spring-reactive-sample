
# Coroutines

Spring WebFlux treats Kotlin coroutines as first-class citizens: a controller method can be a `suspend` function returning a single value, or a regular function returning `Flow<T>` for streams. Spring bridges to and from Reactor internally, so the reactive server stack stays unchanged.

## A suspend controller

From the [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) sample of the spring6-sandbox repository:

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

The [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) sample of this repository shows the same pattern against MongoDB, including a `ProblemDetail`-based exception handler:

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

## CoroutineCrudRepository

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

## Testing coroutines

`kotlinx-coroutines-test` provides `runTest`, and [MockK](https://mockk.io/) provides `coEvery`/`coVerify` for suspending functions — from the sample's `PostControllerTest`:

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

The test dependencies: `kotlinx-coroutines-test`, `com.ninja-squad:springmockk`, and `io.kotest:kotest-assertions-core-jvm`.
