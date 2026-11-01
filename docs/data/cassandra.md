# Accessing Cassandra with Spring Data Cassandra

Cassandra is a wide-column database built for very high write throughput and linear scalability. In the Java ecosystem, its driver exposes a reactive execution model, and Spring makes it easy to use that model without giving up repository-style ergonomics.

In this chapter, we look at the two main patterns:

- using Spring Data Cassandra repositories
- working directly with the native reactive driver through `CqlSession`

The examples below follow the same practical style as the other chapters in this book: small, readable, and close to the way you would build a real application.

## Getting started

The fastest path is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../start/intro.md) chapter. Choose **Spring WebFlux** and **Spring Data for Apache Cassandra**.

If you are adding Cassandra to an existing project, include the following dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```

If you are building a plain Spring project without Boot auto-configuration, use this dependency instead:

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-cassandra</artifactId>
</dependency>
```

You also need a configuration class to enable reactive Cassandra support:

```java
@Configuration
@EnableReactiveCassandraRepositories(basePackageClasses = {CassandraConfig.class})
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    @Value("${cassandra.keyspace-name}")
    String keySpace;

    @Value("${cassandra.contact-points}")
    String contactPoints;

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keySpace)
            .ifNotExists()
            .with(KeyspaceOption.DURABLE_WRITES, true);
        return Arrays.asList(specification);
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return Arrays.asList(DropKeyspaceSpecification.dropKeyspace(keySpace));
    }

    @Override
    protected String getKeyspaceName() {
        return keySpace;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.RECREATE;
    }
}
```

The keyspace setup is straightforward: create the keyspace if it does not already exist, and keep the schema behavior explicit. The repository layer then handles most of the CRUD work for you.

You can explore this example in [data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/data-cassandra).

With Spring Boot, the connection is usually configured automatically from `application.properties`:

```properties
spring.cassandra.contact-points=localhost
spring.cassandra.keyspace-name=blog
spring.cassandra.local-datacenter=datacenter1
```

## Defining the table model

Create a `Post` class that maps to a Cassandra table:

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
class Post {

    @PrimaryKey
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String title;
    private String content;
}
```

The annotations are simple:

1. `@Table` maps the class to a Cassandra table.
2. `@PrimaryKey` identifies the primary key field.
3. `@Builder.Default` gives the record an ID by default.

Unlike MongoDB and some other document stores, Cassandra expects the identifier to be set before insertion. The default value covers that for us.

## Creating a repository

Define a repository interface by extending `ReactiveCassandraRepository`:

```java
interface PostRepository extends ReactiveCassandraRepository<Post, String> {}
```

Once Spring creates the repository bean, it can be injected into any controller or service without additional ceremony.

## Exposing a REST API

A small controller is enough to expose a reactive CRUD API:

```java
@RestController
@RequestMapping("/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping
    public Mono<Post> create(@RequestBody Post post) {
        post.setId(UUID.randomUUID().toString());
        return this.posts.save(post);
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return this.posts.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
        return this.posts.findById(id)
            .map(existing -> {
                existing.setTitle(post.getTitle());
                existing.setContent(post.getContent());
                return existing;
            })
            .flatMap(this.posts::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return this.posts.deleteById(id);
    }
}
```

Notice that the `create` method assigns the ID explicitly. Cassandra does not generate it for you the way some document databases do.

## Seeding sample data

You can use a `CommandLineRunner` to insert a few example records during startup:

```java
public void init() {
    log.info("Start data initialization...");
    this.posts
        .deleteAll()
        .thenMany(
            Flux.just("Post one", "Post two")
                .flatMap(title -> this.posts.save(
                    Post.builder()
                        .title(title)
                        .content("content of " + title)
                        .build()))
        )
        .log()
        .subscribe(
            null,
            null,
            () -> log.info("Data initialization complete.")
        );
}
```

This pattern is useful when you want a clean, reproducible bootstrap for a demo or tutorial application.

## Running the application

Before starting the app, make sure Cassandra is running locally.

If you prefer Docker, you can start it directly from the repository's compose file:

```bash
docker-compose up cassandra
```

Then launch the application:

```bash
mvn spring-boot:run
```

You can test the API with `curl`:

```bash
curl -v http://localhost:8080/posts
```

## Working with the native client

The `CqlSession` that Boot auto-configures can be used directly, without Spring Data repositories. Cassandra's driver includes native reactive support — the `executeReactive` extension returns a `ReactiveResultSet`, which converts to Reactor types with `Mono.from(...)`.

### Project setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cassandra</artifactId>
</dependency>
```

### Implementing a repository with `CqlSession`

Switch from the blocking `cqlSession.execute(...)` to `cqlSession.executeReactive(...)`, and leverage `Mono.from(...)` to transform the driver's publisher stream into a reactive flow:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final CqlSession cqlSession;

    public Mono<Product> save(Product product) {
        var id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        String query = """
                INSERT INTO products(id, name, price)
                VALUES (:id, :name, :price)
                IF NOT EXISTS
                """;
        ReactiveResultSet resultSet = cqlSession.executeReactive(query, Map.of(
                "id", id,
                "name", product.name(),
                "price", product.price()
        ));

        return Mono.from(resultSet)
                .flatMap(row -> {
                    log.info("Inserted product with id {}", id);
                    if (row.wasApplied()) {
                        return Mono.just(new Product(id, product.name(), product.price()));
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
```

## Testing with Testcontainers

Define a `CassandraContainer` bean with Spring Boot's `@ServiceConnection` to automatically configure the connection to the Cassandra container:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    CassandraContainer cassandraContainer() {
        return new CassandraContainer(DockerImageName.parse("cassandra:latest"))
                .withInitScript("init.cql")
                .withStartupTimeout(Duration.ofMinutes(5));
    }
}
```

Add the following dependencies to your `pom.xml` for testing with Testcontainers:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-cassandra</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Assert the reactive pipeline with `StepVerifier`:

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        String insertedId = UUID.randomUUID().toString();

        productRepository.save(new Product(insertedId, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    assertThat(product.id()).isEqualTo(insertedId);
                    assertThat(product.name()).isEqualTo("test");
                })
                .verifyComplete();
    }
}
```

You can explore the native-client example in [boot-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cassandra), and the repository-based one in [boot-data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-cassandra).
