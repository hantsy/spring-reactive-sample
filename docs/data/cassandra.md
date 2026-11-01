# Accessing Cassandra with Spring Data Cassandra

Cassandra is a wide-column distributed database built for high write throughput and linear scalability. Its Java driver exposes a reactive execution model natively. In Spring, you can work with Cassandra in two common ways:

- by using Spring Data Cassandra repositories
- by working directly with the driver's `CqlSession` and its reactive `executeReactive` extension

This chapter focuses on the reactive ecosystem and shows how to build a small, practical application using Spring WebFlux and Cassandra.

## Getting started

The quickest way to start is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../start/intro.md) chapter. Select **Spring WebFlux** and **Spring Data for Apache Cassandra**.

If you are adding Cassandra to an existing project, include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```

If you are building a plain Spring project without Boot auto-configuration, use the following dependency instead:

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-cassandra</artifactId>
</dependency>
```

In that case, you also need a configuration class to enable reactive Cassandra support:

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
        //.withNetworkReplication(DataCenterReplication.dcr("foo", 1), DataCenterReplication.dcr("bar", 2));

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

The `getKeyspaceCreations` method configures how the keyspace is created when the application starts — here it creates the keyspace if it does not exist. The `getSchemaAction` method specifies the schema generation behavior.

You can explore this setup in [data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/data-cassandra).

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

    @PrimaryKey()
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String title;
    private String content;
}
```

The annotations in this example are straightforward:

1. `@Table` marks the class as a Cassandra table mapping.
2. `@PrimaryKey` identifies the primary key field.
3. `@Builder.Default` provides a generated ID by default.

Unlike Mongo and other NoSQL stores, Cassandra requires you to fill the `id` field before inserting — the `@Builder.Default` value covers it.

## Creating a repository

Define a repository interface by extending `ReactiveCassandraRepository`:

```java
interface PostRepository extends ReactiveCassandraRepository<Post, String> {}
```

Once Spring creates the repository bean, you can inject it into controllers or services.

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

    @GetMapping("")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
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
            .map(p -> {
                p.setTitle(post.getTitle());
                p.setContent(post.getContent());

                return p;
            })
            .flatMap(p -> this.posts.save(p));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return this.posts.deleteById(id);
    }
}
```

Note how `create` assigns the ID explicitly — as mentioned above, Cassandra does not generate it for you.

## Seeding sample data

You can use a `CommandLineRunner` to insert a few sample records when the application starts:

```java
public void init() {
    log.info("start data initialization  ...");
    this.posts
        .deleteAll()
        .thenMany(
            Flux
                .just("Post one", "Post two")
                .flatMap(
                    title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
                )
        )
        .log()
        .subscribe(
            null,
            null,
            () -> log.info("done initialization...")
        );
}
```

`CommandLineRunner` ensures the initialization logic runs once the application has started.

## Running the application

Before starting the app, make sure Cassandra is running locally.

If you prefer Docker, it is easy to start Cassandra with the Docker Compose file in the repository root:

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
