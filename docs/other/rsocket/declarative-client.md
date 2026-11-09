# Declarative RSocket client

The [WebClient chapter](../../web/httpexchange.md) showed how `@HttpExchange` turns an annotated interface into an HTTP client proxy. RSocket has the same pattern: annotate an interface with `@RSocketExchange`, and `RSocketServiceProxyFactory` creates a proxy that sends each method call as an RSocket request.

Spring Boot auto-configures the surrounding infrastructure — the RSocket server, the Jackson CBOR strategies, and an `RSocketRequester.Builder`. The proxy factory itself is a single bean you declare by hand.

The complete example lives in [boot-rsocketexchange](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-rsocketexchange): an RSocket server backed by PostgreSQL through R2DBC, and a declarative client calling it over TCP.

## Getting started

Add the RSocket and R2DBC starters:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-rsocket</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-r2dbc</artifactId>
</dependency>
```

Configure the server transport and the database in `application.properties`:

```properties
spring.rsocket.server.port=7000
spring.rsocket.server.transport=tcp
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/blogdb
spring.r2dbc.username=user
spring.r2dbc.password=password
spring.sql.init.mode=always
```

Boot starts the RSocket server on port 7000 and registers the messaging infrastructure. The `schema.sql` and `data.sql` scripts initialize the database, as in the [R2DBC chapter](../../data/r2dbc.md).

## Serving routes with @MessageMapping

The server side is a regular `@Controller` with `@MessageMapping` routes, as introduced in the [RSocket over TCP](tcp.md) chapter. Each route delegates to a `DatabaseClient`-based repository:

```java
@Controller
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @MessageMapping("posts.findAll")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @MessageMapping("posts.findById.{id}")
    public Mono<Post> get(@DestinationVariable("id") UUID id) {
        return this.posts.findById(id);
    }

    @MessageMapping("posts.save")
    public Mono<UUID> create(@Payload Post post) {
        return this.posts.save(post);
    }

    // titleContains, update, deleteById follow the same pattern
}
```

## Declare the client interface

The client interface mirrors the server routes. `@RSocketExchange` sets the destination pattern; `@DestinationVariable` and `@Payload` bind the method arguments, and the reactive return type selects the interaction model:

```java
public interface PostClient {

    @RSocketExchange("posts.findAll")
    Flux<Post> all();

    @RSocketExchange("posts.titleContains")
    Flux<Post> titleContains(@Payload String title);

    @RSocketExchange("posts.findById.{id}")
    Mono<Post> get(@DestinationVariable("id") UUID id);

    @RSocketExchange("posts.save")
    Mono<UUID> create(@Payload Post post);

    @RSocketExchange("posts.update.{id}")
    Mono<Boolean> update(@DestinationVariable("id") UUID id, @Payload Post post);

    @RSocketExchange("posts.deleteById.{id}")
    Mono<Boolean> delete(@DestinationVariable("id") UUID id);
}
```

## Create the proxy

Build the proxy from the auto-configured `RSocketRequester.Builder`:

```java
@Configuration(proxyBeanMethods = false)
public class ClientConfig {

    @Bean
    @Lazy
    PostClient postClient(RSocketRequester.Builder builder) {
        var requester = builder.tcp("localhost", 7000);
        var proxyFactory = RSocketServiceProxyFactory.builder(requester)
                .blockTimeout(Duration.ofMillis(5000))
                .build();
        return proxyFactory.createClient(PostClient.class);
    }
}
```

Two details matter here:

- `builder.tcp(...)` connects immediately. Boot starts the RSocket server after bean instantiation, so the bean is `@Lazy` — the connection opens on first use, when the server is already listening.
- No manual `RSocketStrategies` configuration is needed. Boot registers the Jackson 3 JSON and CBOR codecs by default, and both sides of this example share them.

From this point on, inject `PostClient` like any other bean:

```java
client.create(Post.of("test", "content"))
        .flatMap(id -> client.get(id))
        .subscribe(post -> log.debug("post: {}", post));
```

## Testing

The tests run against a real PostgreSQL instance started by Testcontainers. A `@ServiceConnection` bean replaces the `spring.r2dbc.*` settings, following the pattern from the [R2DBC chapter](../../data/r2dbc.md):

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
    }
}
```

The integration test loads the full application — server, database, and client proxy — and drives the routes through the declarative interface:

```java
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class IntegrationTests {

    @Autowired
    PostClient client;

    @Test
    public void testCreateAndGet() {
        var data = Post.of("declarative client", "content of the declarative client post");

        this.client.create(data)
                .flatMap(id -> this.client.get(id))
                .as(StepVerifier::create)
                .consumeNextWith(post -> {
                    assertThat(post.title()).isEqualTo(data.title());
                    assertThat(post.status()).isEqualTo(Status.DRAFT);
                })
                .verifyComplete();
    }
}
```

See the complete example and tests in [boot-rsocketexchange](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-rsocketexchange).