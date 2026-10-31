
# Spring Test Context and Slices

Spring Test provides a spectrum of test contexts, from a targeted slice to the full application. This page maps the spectrum as the samples use it: `SpringJUnitConfig` for plain Spring modules, `@SpringBootTest` for full Boot contexts, and the Boot test slices that load only what a layer needs.

## SpringJUnitConfig: plain Spring without Boot

Non-Boot samples test with `SpringJUnitConfig` — the JUnit 5 extension that builds an `ApplicationContext` from the given configuration classes without any Boot machinery:

```java
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")   // disables the Netty @Profile("default") server bean
public class ApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient rest;

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
                .bindToApplicationContext(this.context)
                .configureClient()
                .build();
    }

    @Test
    public void getAllPostsWillBeOk() {
        this.rest
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }
}
```

This pattern runs across the plain Spring samples — the [integration](https://github.com/hantsy/spring-reactive-sample/tree/master/integration) sample's `ApplicationTests` and `PostControllerTest` show it with `bindToApplicationContext` and the narrower `bindToController`:

```java
this.rest = WebTestClient.bindToController(this.ctrl)
        .configureClient()
        .build();
```

## SpringBootTest: the full context

`@SpringBootTest` boots the complete application. With `WebEnvironment.RANDOM_PORT` it starts the real server on a free port, and `@AutoConfigureWebTestClient` injects a client ready to talk to it:

```java
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    @Autowired
    WebTestClient client;

    @Test
    public void getAllPosts() {
        client.get().uri("/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(2);
    }
}
```

Combine it with the Testcontainers styles from [Data Operations with Testcontainers](testcontainers.md) (`@ServiceConnection` or `@DynamicPropertySource`) and the full stack — web layer, data layer, real database — runs in one test.

### Integration tests over a real server

Some contracts only exist over the wire. The [integration](https://github.com/hantsy/spring-reactive-sample/tree/master/integration) sample binds its own Reactor Netty server bean and a server-bound `WebTestClient`:

```java
@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMillis(5000))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @AfterEach
    public void tearDown() {
        this.disposableServer.disposeNow();
    }

    @Test
    public void getAllPostsWillBeOk_viaIntegration() {
        this.client
                .get()
                .uri("/all")
                .exchange()
                .expectStatus().isOk();
    }
}
```

This is the shape to use for [SSE](../web/sse.md) streams, [WebSocket](../web/websocket.md) handshakes, and cookie/header contracts — see [session-header](https://github.com/hantsy/spring-reactive-sample/tree/master/session-header) for a worked example.

## Test slices

Boot's test slices load only the auto-configuration a layer needs, keeping tests fast and focused:

| Slice | Loads | Typical use |
| :--- | :--- | :--- |
| `@WebFluxTest` | WebFlux infrastructure for the given controllers | Controller and filter tests with mocked dependencies — see [Testing Web Components](web-components.md) |
| `@JsonTest` | JSON serializers/deserializers only (Jackson, and `@JsonTester` injection) | Round-trip serialization assertions without any web layer |
| `@DataMongoTest` | Spring Data Mongo repositories and `ReactiveMongoTemplate` | Repository tests against a real Mongo — pair with Testcontainers |
| `@DataR2dbcTest` | Spring Data R2DBC repositories, `DatabaseClient` | R2DBC repository tests |
| `@DataNeo4jTest` | Spring Data Neo4j repositories | Neo4j repository tests — see [Neo4j — Testing](../data/neo4j.md#testing) |

A `@DataMongoTest` repository test from the [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo) sample — the slice plus a `@ServiceConnection` container gives a complete database test with no web layer loaded:

```java
@DataMongoTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;
}
```

!!! note "Slices and transactions"

    Reactive slices are not transactional by default. When a slice (for example `@DataNeo4jTest`) applies test-managed transactions that the reactive stack cannot drive, add `@Transactional(propagation = Propagation.NEVER)` — the [Neo4j — Testing](../data/neo4j.md#testing) page walks through the details.

A `@JsonTest` follows the same recipe — the slice auto-configures the JSON beans and injects a tester:

```java
@JsonTest
class PostJsonTest {

    @Autowired
    JacksonTester<Post> json;

    @Test
    void serialize() throws Exception {
        var post = new Post(UUID.randomUUID(), "test title", "content");
        assertThat(json.write(post)).extractingJsonPathStringValue("$.title").isEqualTo("test title");
    }
}
```

The slices pair naturally with the bean-override annotations: `@WebFluxTest` + `@MockitoBean` for the web layer, data slices + Testcontainers for the persistence layer.
