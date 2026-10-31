
# Testing

Testing reactive code needs tools that understand asynchrony: assertions on sequences of signals rather than single values. The reactive stack ships two complementary utilities — `StepVerifier` for publisher-level tests and `WebTestClient` for HTTP-level tests — plus the usual Spring Test support and Testcontainers for integration tests. Every sample in the repository uses one or more of these.

## StepVerifier: verify a Publisher

`StepVerifier` (from the `reactor-test` module) drives a `Publisher` and asserts the exact sequence of signals:

```java
StepVerifier.create(posts.findAll())
    .consumeNextWith(p -> assertEquals("post one", p.getTitle()))
    .consumeNextWith(p -> assertEquals("post two", p.getTitle()))
    .expectComplete()
    .verify();
```

Common patterns used across the samples:

```java
// assert a count of elements
postClient.allPosts()
        .as(StepVerifier::create)
        .expectNextCount(2)
        .verifyComplete();

// assert with custom assertions and cancellation
counter.getInvocationCount()
        .as(StepVerifier::create)
        .consumeNextWith(count -> assertThat(count).isGreaterThan(0))
        .thenCancel()
        .verify();

// combine into the builder chain
posts.findByTitleLike("one")
    .as(StepVerifier::create)
    .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
    .verifyComplete();
```

## WebTestClient: test the HTTP layer

`WebTestClient` mirrors the `WebClient` API and adds response assertions. The samples use it in three binding modes.

### Bind to the application context

Loads the full context and exercises the request handling stack without a socket:

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

### Bind to a controller

Tests a single controller in isolation:

```java
@WebFluxTest(controllers = MessageController.class)
public class DemoApplicationTests {

    @Autowired
    WebTestClient client;

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/").exchange().expectStatus().isOk();
    }
}
```

### Bind to a running server

Boots the real Reactor Netty server and tests over the wire — used when the test needs the actual HTTP contract (SSE, WebSocket, cookies):

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
}
```

JSON payloads are asserted with JSONPath:

```java
this.rest
        .get()
        .uri("/posts")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2);
```

## Mocking the HTTP edge with WireMock

For client-side tests, the [WebClient samples](../web/client.md#testing-a-webclient-with-wiremock) run a [WireMock](https://wiremock.org/) server on the port the client targets and stub each endpoint. See the `client` sample's `PostClientTest` for the full pattern.

## Integration tests with Testcontainers

Store-backed samples test against real databases in Docker. Two wiring styles appear in the repository:

### ApplicationContextInitializer + @SpringJUnitConfig

The plain Spring samples (cache, R2DBC) implement the initializer pattern — start the container, map its properties, stop it when the context closes:

```java
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18.4-alpine"));

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        context.addApplicationListener((ContextClosedEvent event) -> POSTGRES.stop());
        TestPropertyValues.of(
                "r2dbc.host=" + POSTGRES.getHost(),
                "r2dbc.port=" + POSTGRES.getFirstMappedPort(),
                "r2dbc.username=" + POSTGRES.getUsername(),
                "r2dbc.password=" + POSTGRES.getPassword(),
                "r2dbc.database=" + POSTGRES.getDatabaseName()
        ).applyTo(context.getEnvironment());
    }
}

@ContextConfiguration(initializers = { ContainersConfiguration.class })
@SpringJUnitConfig(classes = { PostRepositoryTest.TestConfig.class })
public class PostRepositoryTest { /* ... */ }
```

### Spring Boot @ServiceConnection

The Spring Boot samples let Boot map the connection automatically:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    }
}

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests { /* ... */ }
```

## Test slices

Spring Boot provides reactive test slices that load only the configuration a test needs — `@WebFluxTest` for controllers, `@DataMongoTest` for Mongo repositories, and friends. For repository slices that need a real store, combine the slice with Testcontainers as shown above; see [Spring Data Neo4j — Testing](../data/data-neo4j.md#testing) for a worked example with `@DataNeo4jTest`.
