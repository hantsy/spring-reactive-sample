
# Spring Data Cassandra

Spring Data Cassandra adds basic Reactive support.

Follow the [Getting Started](../start/intro.md) chapter to create a freestyle (no Spring Boot) or Spring Boot based project skeleton.

If you are not using Spring Boot, first add the following dependencies to your project.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-cassandra</artifactId>
</dependency>
```

Create a `@Configuration` class to configure Cassandra and add `@EnableReactiveCassandraRepositories` to enable reactive `Repository` support.

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

The `getKeyspaceCreations` configures how to create the keyspace when Cassandra is started, here we create the keyspace if it does not existed.

The `getSchemaAction` specifies the action of schema generation.

Create a `Post` class to present the table in Cassandra, and add add a `@Table` annotation on this class.

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

Add `@PrimaryKey` on `id` field, it indicates `id` is the primary key of `posts` table.

!!! note

    Unlike Mongo and other NoSQL, in Cassandra, you have to fill the `id` field manually before it is inserted.

Next change the former `PostRepository` to the following:

```java
interface PostRepository extends ReactiveCassandraRepository<Post, String>{}
```

For the complete codes, check [spring-reactive-sample/data-cassandra](https://github.com/hantsy/spring-reactive-sample/blob/master/data-cassandra).

Alternatively, for Spring Boot applications, just need to add `spring-boot-starter-data-cassandra-reactive` into your project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```

No need extra configuration, Spring Boot will autoconfigure Cassandra and registers all essential beans for you.

For the complete codes, check [spring-reactive-sample/boot-data-cassandra](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-cassandra).

Add some sample data via a `CommandLineRunner` bean or a `ApplicationRunner` bean.

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

## Working with the native client

In a Spring Boot 4 application the `spring-boot-starter-cassandra` starter auto-configures a native `CqlSession` directly — no Spring Data repository required. Under blocking settings, you execute queries synchronously, but in a reactive application you use the reactive driver extension `executeReactive`, which returns a `ReactiveResultSet`.

### Project setup

Generate a new project at [Spring Initializr](https://start.spring.io/) with **Cassandra**, **WebFlux**, **Lombok**, and **Testcontainers**:

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

The starter auto-configures the connection pool and exposes the `CqlSession` bean. Cassandra's driver includes native reactive support, so no additional reactive driver is needed.

### Implementing a thin repository

Switch from the blocking `cqlSession.execute(...)` to `cqlSession.executeReactive(...)`, and leverage Project Reactor's `Mono.from(...)` to transform the driver's publisher stream into a reactive flow:

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

### Testing with Testcontainers

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

Define the container using Spring Boot's `@ServiceConnection` model:

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

The working example lives in the [boot-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cassandra) module.
