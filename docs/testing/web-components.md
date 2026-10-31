
# Testing Web Components

The web layer is tested with `WebTestClient`, which mirrors the `WebClient` API and adds response assertions. This page covers testing annotated controllers, exception handling, `RouterFunction`s, and filters — with the dependencies mocked or spied through Spring's bean overriding annotations.

The tests run on JUnit 5 with Mockito for stubbing; for the Kotlin equivalents see [Testing in Kotlin](kotlin.md).

## A controller test with a mocked dependency

`@WebFluxTest(controllers = ...)` loads only the web configuration for the given controller — no database, no full context. The controller's dependencies are replaced with `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`), and Mockito stubs their publisher-returning methods:

```java
@WebFluxTest(controllers = PostController.class)
public class PostControllerTest {

    @Autowired
    WebTestClient client;

    @MockitoBean
    PostRepository posts;

    @Test
    public void getAllPosts() {
        when(this.posts.findAll()).thenReturn(Flux.just(
                Post.builder().id(UUID.randomUUID()).title("post one").content("content of post one").build(),
                Post.builder().id(UUID.randomUUID()).title("post two").content("content of post two").build()
        ));
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(2);

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }
}
```

Notes:

- `@WebFluxTest` auto-configures a `WebTestClient` bound to the mock web environment — inject it with `@Autowired`.
- `@MockitoBean` replaces the bean in the context with a Mockito mock; `when(...)`/`given(...)` stub the reactive methods to return `Flux.just(...)` or `Mono.just(...)`.
- The BDD style works too: `given(this.posts.findAll()).willReturn(Flux.just(post1, post2))`.

!!! note "Boot 3 vs Boot 4"

    Spring Boot 3 used `@MockBean` (deprecated and removed in Boot 4); Spring Framework 6.2+ / Boot 4 use `@MockitoBean` and `@MockitoSpyBean` from `org.springframework.test.context.bean.override.mockito`.

## Mocks and spies

- `@MockitoBean` — replace a bean entirely with a mock. Unstubbed publisher-returning methods return empty publishers.
- `@MockitoSpyBean` — wrap the real bean in a spy: real methods run unless explicitly stubbed, and interactions are still verifiable. Use it when the collaborator's real behavior matters and you only need to observe or tweak a few calls:

```java
@MockitoSpyBean
PostRepository posts;
```

Both annotations live in the same package as `@MockitoBean` and work in any Spring test — slices, `@SpringBootTest`, or plain `SpringJUnitConfig` tests.

## Exception handling and status codes

Exception handlers are exercised by driving the client into the error path and asserting the mapped status — here a not-found domain exception maps to 404 through the controller's `@ExceptionHandler`:

```java
@Test
fun `get post that not found`() = runTest {
    val id = 1L
    coEvery { postRepository.findById(any()) } throws PostNotFoundException(id)

    client.get().uri("/$id")
        .exchange()
        .expectStatus().isNotFound

    coVerify(exactly = 1) { postRepository.findById(any()) }
}
```

The Java pattern is identical with Mockito: stub the dependency to return `Mono.error(new PostNotFoundException(id))`, then `expectStatus().isNotFound()`. Assert the body of RFC 9457 `ProblemDetail` responses with `expectBody(ProblemDetail.class)` when a handler returns one.

## Testing RouterFunctions

Functional routes are tested by binding the client to the `RouterFunction` bean. From the [boot-start-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start-routes) sample:

```java
@AutoConfigureWebTestClient
@SpringBootTest
// create a `WebTestClient` with essential options from application context automatically,
// it works on both mock env or a real server.
public class ApplicationTest {

    @Autowired
    RouterFunction<ServerResponse> routerFunction;

    @MockitoBean
    PostRepository posts;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToRouterFunction(this.routerFunction)
                .configureClient()
                .build();
    }

    @Test
    public void getAllPosts() {
        // stub the repository, call client.get().uri("/posts")...
    }
}
```

`bindToRouterFunction` handles a single `RouterFunction`; `bindToApplicationContext(context)` loads the full handler infrastructure (filters, exception handlers) without a socket, and `bindToWebHandler(...)` sits between the two.

## Testing filters

Filters are verified by importing them into a `@WebFluxTest` slice and asserting the outcome with and without the triggering condition — the [spring6-sandbox](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) filter tests do exactly that for a security-check filter:

```java
@WebFluxTest(controllers = HelloController.class)
class WebFilterTests {

    @TestConfiguration
    @Import(value = SecurityWebFilter.class)
    class TestConfig

    @Autowired
    WebTestClient client;

    @Test
    void `authentication failed with no user params`() = runTest {
        client.get().uri("/hello")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    void `authentication passed with user params`() = runTest {
        client.get().uri("/hello?user=Hantsy")
            .exchange()
            .expectStatus().isOk
    }
}
```

A Java `WebFilter` test follows the same shape: `@Import` the filter, request a route the filter protects, assert 401 without the precondition and 200 with it.

## Against a running server

Slice tests exercise the request-handling stack only. When a test needs the real HTTP contract — SSE, WebSocket, cookies — bind the client to a live server instead; see [Spring Test Context and Slices](spring-test.md#integration-tests-over-a-real-server).
