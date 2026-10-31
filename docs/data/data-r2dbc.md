
# Spring Data R2DBC

R2DBC (Reactive Relational Database Connectivity) brings non-blocking access to relational databases. Spring Data R2DBC provides familiar Spring Data abstractions on top of it: `DatabaseClient` for SQL, `R2dbcRepository` for repositories, and a `ReactiveTransactionManager` for transactions.

!!! note "Dedicated repository"

    Spring Data R2DBC 1.2 introduced many breaking changes, so a separate repository — [hantsy/spring-r2dbc-sample](https://github.com/hantsy/spring-r2dbc-sample) — tracks the current features in depth. The legacy samples of this repository live under [legacy/data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/data-r2dbc) and related `legacy/boot-data-r2dbc-*` modules.

This guide walks through a working R2DBC setup step by step, using the [cache](https://github.com/hantsy/spring-reactive-sample/tree/master/cache) sample (PostgreSQL + R2DBC) as the reference.

## Step 1: add the dependencies

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
</dependency>
```

With Spring Boot, use the starter instead:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
```

## Step 2: configure the connection factory

For a non-Boot application, create a configuration class that extends `AbstractR2dbcConfiguration` and declares the `ConnectionFactory`:

```java
@Configuration
@EnableR2dbcRepositories
@EnableTransactionManagement
@Slf4j
public class DataR2dbcConfig extends AbstractR2dbcConfiguration {

    @Value("${r2dbc.host}")
    String host;
    @Value("${r2dbc.port}")
    Integer port;
    @Value("${r2dbc.database}")
    String databaseName;
    @Value("${r2dbc.username}")
    String userName;
    @Value("${r2dbc.password}")
    String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port)
                        .database(databaseName)
                        .username(userName)
                        .password(password)
                        .build()
        );
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```

`AbstractR2dbcConfiguration` registers a `DatabaseClient` and wires the reactive repository infrastructure; `@EnableR2dbcRepositories` activates it for the package of the configuration class.

With Spring Boot, no configuration class is needed — set the connection in `application.properties`:

```properties
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/blogdb
spring.r2dbc.username=user
spring.r2dbc.password=password
```

## Step 3: initialize the schema

R2DBC has no schema generation, so provide a `schema.sql` (and optionally `data.sql`) and register a `ConnectionFactoryInitializer`:

```java
@Bean
ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);

    CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
    populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
    populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("data.sql")));
    initializer.setDatabasePopulator(populator);

    return initializer;
}
```

With `schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS posts (
    id UUID DEFAULT uuid_generate_v4(),
    title VARCHAR(255),
    content VARCHAR(255),
    PRIMARY KEY (id)
);
```

## Step 4: define the entity and repository

R2DBC entities map relational columns; the sample uses a Java record:

```java
@Table("posts")
public record Post(@Id UUID id,
                   String title,
                   String content
) {
    public static Post of(String title, String content) {
        return new Post(null, title, content);
    }
}
```

The repository interface extends `R2dbcRepository`:

```java
public interface PostRepository extends R2dbcRepository<Post, UUID> {
}
```

Like the reactive repositories of other stores, all query methods return `Mono` or `Flux`, and there is no `findAll(Pageable)` — use Reactor's `skip`/`take` or derive a paged query with a `Pageable` parameter.

## Step 5: use the repository

```java
var posts = context.getBean(PostRepository.class);

// save
posts.save(Post.of("What is new in Spring", "An introduction to new features"))
    .thenMany(posts.findAll())
    .subscribe(post -> log.debug("get the initialized data: {}", post));
```

## Transactions

The `R2dbcTransactionManager` bean (Step 2) enables `@Transactional` on reactive methods:

```java
@Transactional
public Mono<Void> saveAllWithTransaction(List<Post> data) {
    return Flux.fromIterable(data)
            .flatMap(repository::save)
            .then();
}
```

## Combining R2DBC with caching

Because repository methods return `Mono`/`Flux`, they compose with Spring's reactive caching support (Spring 6.1+). The [cache](https://github.com/hantsy/spring-reactive-sample/tree/master/cache) sample annotates the repository methods directly — see [Caching](../other/cache.md) for the full configuration of the in-memory, Caffeine, and Redis cache managers.

```java
public interface PostRepository extends R2dbcRepository<Post, UUID> {
    @Override
    @Cacheable(value = "posts", key = "#p0")
    Mono<Post> findById(UUID id);

    @Override
    @CachePut(value = "posts", key = "#result.id")
    Mono<Post> save(Post post);

    @Override
    @CacheEvict(value = "posts", key = "#p0")
    Mono<Void> deleteById(UUID id);
}
```

## DatabaseClient

For hand-written SQL, inject the `DatabaseClient` that `AbstractR2dbcConfiguration` (or Boot auto-configuration) provides:

```java
@Component
@RequiredArgsConstructor
public class PostRepository {

    private final DatabaseClient databaseClient;

    Flux<Post> findByTitleContains(String title) {
        return this.databaseClient.sql("SELECT * FROM posts WHERE title LIKE :title")
                .bind("title", "%" + title + "%")
                .map(row -> new Post(
                        row.get("id", UUID.class),
                        row.get("title", String.class),
                        row.get("content", String.class)))
                .all();
    }
}
```

## Testing with Testcontainers

The samples run integration tests against a PostgreSQL Testcontainer without any manual port wiring:

```java
@ContextConfiguration(initializers = { ContainersConfiguration.class })
@SpringJUnitConfig(classes = { PostRepositoryTest.TestConfig.class })
public class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @Test
    public void testSaveAll() {
        var data = Post.of("test", "content");
        var post = posts.save(data).block();

        var firstCalled = posts.findById(post.id()).block();
        assertThat(firstCalled).isNotNull();

        posts.deleteById(post.id()).block();
    }
}
```

Where `ContainersConfiguration` starts the container and maps its properties:

```java
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18.4-alpine"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

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
```

To run the sample application itself, start a PostgreSQL server with Docker:

```bash
docker-compose up postgres
```

Then run the sample as a plain Java application (the samples use manual wiring — see [Getting Started](../start/intro.md)).

## Further resources

- [Spring Data R2DBC reference documentation](https://docs.spring.io/spring-data/r2dbc/reference/)
- [hantsy/spring-r2dbc-sample](https://github.com/hantsy/spring-r2dbc-sample) — dedicated repository with the latest R2DBC features
- The legacy samples: [data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/data-r2dbc), [boot-data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc), [boot-data-r2dbc-auditing](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc-auditing), and the PostgreSQL, MySQL, and MS SQL variants
