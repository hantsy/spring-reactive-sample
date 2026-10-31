
# Testing in Kotlin

Kotlin tests keep the same layered structure as the Java ones — `StepVerifier`, `WebTestClient`, Testcontainers — with a Kotlin-native toolkit on top: [Kotest](https://kotest.io/) assertion matchers, [MockK](https://mockk.io/) for suspending functions, [Spring MockK](https://github.com/Ninja-Squad/springmockk) for bean overrides, and `kotlinx-coroutines-test` for coroutine lifecycles.

The samples referenced here are [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) and [boot-kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co) in this repository, plus [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) in the spring6-sandbox repository.

## Test dependencies

```xml
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.kotest</groupId>
    <artifactId>kotest-assertions-core-jvm</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.mockk</groupId>
    <artifactId>mockk-jvm</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.ninja-squad</groupId>
    <artifactId>springmockk</artifactId>
    <scope>test</scope>
</dependency>
```

## MockK: mocking suspending functions

MockK mirrors the Mockito vocabulary for coroutines: `coEvery` stubs a `suspend` call, `coVerify` asserts it, and `coEvery { ... } throws ...` drives the error paths:

```kotlin
coEvery { postRepository.findByTitleContains(any()) } returns
        flowOf(Post(id = 1, title = "test title", content = "test content"))

coEvery { postRepository.findById(any()) } throws PostNotFoundException(id)

coVerify(exactly = 0) { postRepository.findByTitleContains(any()) }
```

`Flow`-returning methods are stubbed with `flowOf(...)`, and `clearAllMocks()` resets between tests.

## Spring MockK: @MockkBean and @MockkSpyBean

[Spring MockK](https://github.com/Ninja-Squad/springmockk) is the Kotlines counterpart of `@MockitoBean`/`@MockitoSpyBean` (see [Testing Web Components](web-components.md)):

- `@MockkBean` — replace a bean with a MockK mock; stub it with `coEvery { ... } returns ...`
- `@MockkSpyBean` — wrap the real bean in a spy: real behavior by default, `coEvery` overrides selected calls

The [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) sample uses `@MockkBean` inside a `@WebFluxTest` slice:

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

## kotlinx-coroutines-test: runTest

Every test that touches coroutine code runs under `runTest { }` — it skips real delays and completes when the coroutine tree settles, even for the `WebTestClient` exchange calls above. The repository tests use the same shape:

```kotlin
@Test
fun `test save and query`() = runTest {
    val saved = template.insert(Post(title = "test", content = "content of test")).awaitSingle()
    saved.id shouldNotBe null

    val postById = posts.findById(saved.id!!)
    postById shouldNotBe null
    postById!!.title shouldBe "test"
}
```

The `awaitSingle()` extension (from `kotlinx.coroutines.reactor`) bridges the reactive `Mono` into the coroutine; `awaitSingleOrNull()`, `awaitFirst()`, and `awaitToList()` cover the other shapes — see the transaction extensions in [Coroutines](../kotlin/coroutines.md#transactions-in-coroutines).

## Kotest assertions

Kotest contributes the infix assertion matchers used across the Kotlin samples — `shouldBe`, `shouldNotBe`, `shouldContain`, and friends:

```kotlin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

posts shouldNotBe null
postById!!.title shouldBe "test"
```

Kotest matchers compose with everything above — inside `runTest`, inside `StepVerifier.consumeNextWith`, or alongside AssertJ in mixed codebases. They work on plain JUnit 5 tests (as the samples do) or inside Kotest's own spec styles if you prefer a full Kotest test framework.

## A complete plain Spring test

The [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) sample assembles the pieces — `SpringJUnitConfig`, a manually registered MockK bean, `bindToController`, and Kotest matchers — without Boot:

```kotlin
@SpringJUnitConfig(classes = [TestConfig::class])
class PostControllerTest {

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = [PostController::class])
    internal class TestConfig {

        @Bean
        @Primary
        fun mockPostRepository(): PostRepository = mockk<PostRepository>()
    }

    @Autowired
    lateinit var posts: PostRepository

    lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToController(PostController(posts))
            .configureClient()
            .build()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `get post by id`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { posts.findById(any()) } returns
                Post(id = id, title = "test title", content = "content of test title")

        client.get().uri("/posts/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.title").isEqualTo("test title")
    }
}
```

When Boot's bean-override machinery is available, prefer `@MockkBean` over the manual `@Primary` mock bean — the manual registration shown here is what makes the same test work in plain Spring modules.
