# Database Client Modules

In Spring Boot 4, the monolithic autoconfiguration has been modularized. Instead of importing full-featured Spring Data repositories, you can pull in lightweight feature-specific starters to work directly with database drivers or client SDKs. This allows you to construct lightweight data access layers or perform lower-level database operations directly.

This guide walks through how to configure and use database client SDKs reactively, contrasting the synchronous/blocking APIs with their reactive counterparts, and demonstrates how to test them using Testcontainers.

---

## Cassandra `CqlSession`

For Cassandra, the starter `spring-boot-starter-cassandra` auto-configures a native `CqlSession`. Under blocking settings, you execute queries synchronously, but in a reactive application, you utilize the reactive driver extension `executeReactive` which returns a `ReactiveResultSet`.

### Dependency Configuration

To use the Cassandra native client SDK in your project, add the following starter dependency to your `pom.xml`. This configures the connection pool and exposes the `CqlSession` bean:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cassandra</artifactId>
</dependency>
```

### Implementing Cassandra Repositories

In a traditional synchronous application, database operations blocking the execution thread are common. Here is how a synchronous repository implementation using `CqlSession` looks:

```java
@Component
@RequiredArgsConstructor
public class ProductRepository {
    private final CqlSession cqlSession;

    public Product save(Product product) {
        var id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        String query = """
                INSERT INTO products(id, name, price)
                VALUES (:id, :name, :price)
                """;
        cqlSession.execute(query, Map.of("id", id, "name", product.name(), "price", product.price()));
        return new Product(id, product.name(), product.price());
    }
}
```

To adapt this to a reactive stack, we switch to `cqlSession.executeReactive()` which returns a `ReactiveResultSet`. We can then leverage Project Reactor's `Mono.from(...)` to transform the driver's publisher stream into a reactive flow:

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

To test the reactive Cassandra repository, add the Testcontainers Cassandra module and Spring Boot's Testcontainers support to your `pom.xml` under the `<dependencies>` section:

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

Define the test container configuration using Spring Boot 4's `@ServiceConnection` model to automatically discover and map database ports and credentials:

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

Finally, implement the JUnit test class using the `StepVerifier` to verify the reactive assertions:

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

        productRepository.findById(insertedId)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p.name()).isEqualTo("test");
                })
                .verifyComplete();
    }
}
```

The working example lives in the [boot-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cassandra) module. Start with [ProductRepository.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cassandra/src/main/java/com/example/demo/ProductRepository.java) to understand how `executeReactive` pipelines are constructed, then head to [TestcontainersConfiguration.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cassandra/src/test/java/com/example/demo/TestcontainersConfiguration.java) to see the `CassandraContainer` wiring, and finally step through [DemoApplicationTests.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cassandra/src/test/java/com/example/demo/DemoApplicationTests.java) where `StepVerifier` drives the assertions.

---

## Couchbase `Cluster`

The `spring-boot-starter-couchbase` starter auto-configures a Couchbase `Cluster` bean. By obtaining the collection reference, you can perform document operations. For reactive operations, simply call `.reactive()` on the collection to obtain a `ReactiveCollection`.

### Dependency Configuration

Include the Couchbase starter in your `pom.xml` to automatically configure the SDK and bootstrap the connection cluster:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-couchbase</artifactId>
</dependency>
```

### Implementing Couchbase Repositories

For a standard synchronous Couchbase connection, operations on the collection block until the server acknowledges the transaction:

```java
@Component
@RequiredArgsConstructor
public class ProductRepository {
    private final Cluster cluster;
    private Collection productCollection;

    @PostConstruct
    public void init() {
        this.productCollection = this.cluster.bucket("demo").defaultCollection();
    }

    public Product save(Product product) {
        String id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        this.productCollection.upsert(id, product);
        return new Product(id, product.name(), product.price());
    }
}
```

By requesting the `.reactive()` version of the collection, the Couchbase SDK returns a `ReactiveCollection`. Every operation on this reference yields a Reactor type (`Mono` or `Flux`) without blocking:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final Cluster cluster;
    private ReactiveCollection productCollection;

    @PostConstruct
    public void init() {
        this.productCollection = this.cluster
                .bucket("demo")
                .defaultCollection()
                .reactive();
    }

    public Mono<Product> save(Product product) {
        String id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        return this.productCollection.upsert(id, product)
                .mapNotNull(result -> {
                    log.debug("saving product result: {}", result);
                    return new Product(id, product.name(), product.price());
                });
    }
}
```

### Testing with Testcontainers

To test the reactive Couchbase repository, append the following Testcontainers Couchbase dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-couchbase</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Configure the Couchbase test container with defined buckets and credentials:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public CouchbaseContainer couchbaseContainer() {
        return new CouchbaseContainer("couchbase/server")
                .withCredentials("Administrator", "password")
                .withBucket(new BucketDefinition("demo").withPrimaryIndex(true))
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofSeconds(120));
    }
}
```

Wire the test configuration into the Spring Boot test case:

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        String id = UUID.randomUUID().toString();
        
        productRepository.save(new Product(id, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    assertThat(product.id()).isEqualTo(id);
                })
                .verifyComplete();

        productRepository.findById(id)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p.name()).isEqualTo("test");
                })
                .verifyComplete();
    }
}
```

The complete [boot-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-couchbase) module is worth a read end-to-end. [ProductRepository.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-couchbase/src/main/java/com/example/demo/ProductRepository.java) shows how `.reactive()` unlocks the `ReactiveCollection` API, [TestcontainersConfiguration.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-couchbase/src/test/java/com/example/demo/TestcontainersConfiguration.java) handles bucket provisioning and credential setup for the ephemeral Couchbase server, and [DemoApplicationTests.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-couchbase/src/test/java/com/example/demo/DemoApplicationTests.java) ties it all together with reactive upsert and retrieval assertions.

---

## Elasticsearch `ReactiveElasticsearchClient`

While the synchronous starter `spring-boot-starter-elasticsearch` configures a blocking `ElasticsearchClient`, Spring Data provides the `ReactiveElasticsearchClient` to execute indexing and search operations reactively using Project Reactor types.

### Dependency Configuration

> [!IMPORTANT]
> Unlike other data stores, Elasticsearch is an exception: there is no reactive client in the official Elasticsearch Java Client SDKs. However, Spring Data Elasticsearch provides its own custom reactive client implementation (`ReactiveElasticsearchClient`). As a side effect, you cannot use a lightweight client-only starter; you must import the full Spring Data Elasticsearch starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### Implementing Elasticsearch Repositories

In blocking scenarios, the high-level `ElasticsearchClient` is used to index and retrieve documents, throwing checked exceptions and blocking the thread:

```java
@Component
@RequiredArgsConstructor
public class ProductRepository {
    private final ElasticsearchClient client;

    @SneakyThrows
    public Product save(Product product) {
        var id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        IndexResponse response = client.index(b -> b.id(id).index("products").document(product));
        return new Product(response.id(), product.name(), product.price());
    }
}
```

In a reactive application, inject the custom `ReactiveElasticsearchClient` provided by Spring Data. This allows you to chain document operations in non-blocking Reactor streams:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final ReactiveElasticsearchClient client;

    public Mono<Product> save(Product product) {
        var id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        log.debug("Saving product with id={}", id);
        return client.index(builder -> builder.id(id).index("products").document(product))
                .mapNotNull(response -> {
                    var savedID = response.id();
                    log.debug("Saved product with id={}", savedID);
                    return new Product(savedID, product.name(), product.price());
                });
    }
}
```

### Testing with Testcontainers

To test the reactive Elasticsearch repository, add the Testcontainers Elasticsearch module to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-elasticsearch</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Configure the Elasticsearch container:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.10"));
    }
}
```

Implement the test integration class:

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        String id = UUID.randomUUID().toString();
        
        productRepository.save(new Product(id, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    assertThat(product.id()).isEqualTo(id);
                })
                .verifyComplete();

        productRepository.findById(id)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p.name()).isEqualTo("test");
                })
                .verifyComplete();
    }
}
```

Because `ReactiveElasticsearchClient` comes from Spring Data rather than the official SDK, it is instructive to trace exactly how the integration is wired. Browse [ProductRepository.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-elasticsearch/src/main/java/com/example/demo/ProductRepository.java) to see the index and get operations in action, [TestcontainersConfiguration.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-elasticsearch/src/test/java/com/example/demo/TestcontainersConfiguration.java) to understand how a pinned `ElasticsearchContainer` image is registered via `@ServiceConnection`, and [DemoApplicationTests.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-elasticsearch/src/test/java/com/example/demo/DemoApplicationTests.java) to observe end-to-end reactive document lifecycle assertions. The full project is under [boot-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-elasticsearch).

---

## MongoDB `MongoClient`

The MongoDB driver provides both synchronous and reactive variants. `spring-boot-starter-mongodb` registers a reactive `com.mongodb.reactivestreams.client.MongoClient` bean when in a reactive context. The returned driver collections produce publisher instances that can be wrapped using `Mono.from(...)` or `Flux.from(...)`.

### Dependency Configuration

> [!TIP]
> The default `spring-boot-starter-mongodb` starter transitively imports the synchronous driver. To optimize your classpath for reactive-only MongoDB driver usage, exclude the synchronous driver dependency (`mongodb-driver-sync`) and explicitly declare the reactive streams driver (`mongodb-driver-reactivestreams`):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mongodb</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.mongodb</groupId>
            <artifactId>mongodb-driver-sync</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-reactivestreams</artifactId>
</dependency>
```

### Implementing MongoDB Repositories

Using the traditional synchronous MongoDB Java Driver, the collection returns immediate results from the database:

```java
@Component
@RequiredArgsConstructor
public class ProductRepository {
    private final MongoClient mongoClient;
    private MongoCollection<Product> productsCollection;

    @PostConstruct
    public void init() {
        this.productsCollection = mongoClient.getDatabase("test").getCollection("products", Product.class);
    }

    public Product save(Product product) {
        var result = this.productsCollection.insertOne(product);
        var id = result.getInsertedId().asObjectId().getValue().toHexString();
        return product.withId(id);
    }
}
```

In a reactive application, we use the `mongodb-driver-reactivestreams` driver. The reactive collections return standard Reactive Streams `Publisher` objects, which we convert to Project Reactor's `Mono` or `Flux` using `Mono.from(...)` or `Flux.from(...)`:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final MongoClient mongoClient;
    private MongoCollection<Product> productsCollection;

    @PostConstruct
    public void init() {
        this.productsCollection = mongoClient
                .getDatabase("test")
                .getCollection("products", Product.class);
    }

    public Mono<Product> save(Product product) {
        return Mono.from(this.productsCollection.insertOne(product))
                .mapNotNull(result -> {
                    log.debug("save product result: {}", result);
                    var id = result.getInsertedId().asObjectId().getValue().toHexString();
                    return product.withId(id);
                });
    }
}
```

### Testing with Testcontainers

To test the reactive MongoDB repository, configure the Testcontainers MongoDB module in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-mongodb</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Expose the `MongoDBContainer` bean in your test configuration:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    }
}
```

Use `StepVerifier` to assert that documents are successfully inserted and queryable:

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        productRepository.save(new Product(null, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    assertThat(product.getId()).isNotNull();
                    
                    productRepository.findById(product.getId())
                            .as(StepVerifier::create)
                            .consumeNextWith(p -> {
                                assertThat(p.getName()).isEqualTo("test");
                            })
                            .verifyComplete();
                })
                .verifyComplete();
    }
}
```

MongoDB's native Reactive Streams driver is one of the most mature reactive database drivers available. Explore [ProductRepository.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mongo/src/main/java/com/example/demo/ProductRepository.java) to see how `Mono.from(...)` bridges the raw driver publishers into Reactor, check [TestcontainersConfiguration.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mongo/src/test/java/com/example/demo/TestcontainersConfiguration.java) for the lightweight `MongoDBContainer` setup, and walk through [DemoApplicationTests.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mongo/src/test/java/com/example/demo/DemoApplicationTests.java) to see `StepVerifier` asserting document insertion and retrieval without a single blocking call. The full module is at [boot-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mongo).

---

## Neo4j `Driver`

The official Neo4j java driver supports synchronous, asynchronous, and reactive execution models. To utilize it reactively in Spring, configure a `Driver` instance and request a `ReactiveSession`. It is highly recommended to manage the lifecycle of the reactive session using Project Reactor's `Mono.usingWhen(...)` resource management construct.

### Dependency Configuration

Declare the Neo4j starter in your `pom.xml` to bootstrap the driver:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-neo4j</artifactId>
</dependency>
```

### Implementing Neo4j Repositories

Using the synchronous session model, a repository invokes blocking queries directly within a transaction boundary:

```java
@Component
@RequiredArgsConstructor
public class ProductRepository {
    private final Driver driver;
    private Session session;

    @PostConstruct
    public void init() {
        this.session = driver.session();
    }

    public Product save(Product product) {
        String query = """
                MERGE (p:Product {id: $id})
                ON CREATE SET p.name=$name, p.price=$price
                ON MATCH SET p.name=$name, p.price=$price
                RETURN p.id as id, p.name as name, p.price as price
                """;
        var result = this.session.executeWrite(tc -> tc.run(query, Map.of(
                "id", product.id() != null ? product.id() : UUID.randomUUID().toString(),
                "name", product.name(),
                "price", Values.value(product.price().toString())
        )).single());
        return new Product(result.get("id").asString(), result.get("name").asString(), new BigDecimal(result.get("price").asString()));
    }
}
```

Reactively, we open a `ReactiveSession` and utilize Reactor's `Mono.usingWhen(...)` to guarantee that the reactive session is closed on completion or error. This avoids connection leaks in high-throughput reactive systems:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final Driver driver;

    public Mono<Product> save(Product product) {
        String query = """
                MERGE (p:Product {id: $id})
                ON CREATE SET p.name=$name, p.price=$price
                ON MATCH SET p.name=$name, p.price=$price
                RETURN p.id as id, p.name as name, p.price as price
                """;
        Map<String, Object> parameters = Map.of(
                "id", product.id() != null ? product.id() : UUID.randomUUID().toString(),
                "name", product.name(),
                "price", Values.value(product.price().toString())
        );

        SessionConfig sessionConfig = SessionConfig.builder()
                .withBookmarkManager(driver.executableQueryBookmarkManager())
                .build();

        // Open, execute, and automatically close the reactive session
        return Mono.usingWhen(
                Mono.fromSupplier(() -> driver.session(ReactiveSession.class, sessionConfig)),
                session -> Mono.fromDirect(session.executeWrite(tc -> Mono.from(tc.run(query, parameters))
                        .flatMapMany(ReactiveResult::records)
                        .single()
                        .map(result -> {
                            log.debug("saving product {}", result);
                            return new Product(
                                    result.get("id").asString(),
                                    result.get("name").asString(),
                                    new BigDecimal(result.get("price").asString())
                                );
                        })
                )),
                ReactiveSession::close
        );
    }
}
```

### Testing with Testcontainers

To test the reactive Neo4j repository, add the Testcontainers Neo4j support to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-neo4j</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Add the `Neo4jContainer` definition to your test configuration:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    Neo4jContainer neo4jContainer() {
        return new Neo4jContainer(DockerImageName.parse("neo4j:latest"));
    }
}
```

Finally, implement the test case importing this container configuration:

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        productRepository.save(new Product(null, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    assertThat(product.id()).isNotNull();
                    
                    productRepository.findById(product.id())
                            .as(StepVerifier::create)
                            .consumeNextWith(p -> {
                                assertThat(p.name()).isEqualTo("test");
                            })
                            .verifyComplete();
                })
                .verifyComplete();
    }
}
```

Neo4j's `Mono.usingWhen(...)` pattern is the most distinctive pattern in this guide — it cleanly models resource acquisition, usage, and cleanup without any imperative `try/finally` blocks. See [ProductRepository.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j/src/main/java/com/example/demo/ProductRepository.java) for the full reactive Cypher execution logic, [TestcontainersConfiguration.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j/src/test/java/com/example/demo/TestcontainersConfiguration.java) for the `Neo4jContainer` bootstrap, and [DemoApplicationTests.java](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j/src/test/java/com/example/demo/DemoApplicationTests.java) for graph-aware `StepVerifier` assertions. Everything lives in the [boot-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j) module.

---

## A Note on Redis

You may have noticed that Redis is absent from this guide. That is intentional. Unlike the stores above — where Spring Boot 4 introduced a dedicated lightweight starter (`spring-boot-starter-cassandra`, `spring-boot-starter-mongodb`, etc.) that exposes the native driver bean directly — **Redis does not have an equivalent single-client module**. The reason is that Spring Boot has always supported multiple Redis client libraries, primarily:

- **Lettuce** — the default, fully non-blocking reactive client backed by Netty.
- **Jedis** — a synchronous, thread-safe client often chosen for its simplicity in blocking scenarios.

Because these two SDKs have fundamentally different programming models, Spring Boot 4 cannot pin the autoconfiguration to one canonical reactive client the way it can for Cassandra's `CqlSession` or MongoDB's `MongoClient`. Instead, Redis connectivity continues to be managed through `spring-boot-starter-data-redis` (blocking) and `spring-boot-starter-data-redis-reactive` (reactive with Lettuce), both of which configure a higher-level `ReactiveRedisTemplate` rather than exposing the raw driver directly.

If you need to work with the Lettuce driver directly in a reactive context, you can inject `io.lettuce.core.api.reactive.RedisReactiveCommands` after including `spring-boot-starter-data-redis-reactive`. The `data-redis` chapters in this documentation cover Spring Data Redis repository and template usage in depth.

---

## Summary

Spring Boot 4's modularized autoconfiguration makes it practical to depend on a database's native driver or client SDK without pulling in the entire Spring Data abstraction. Here is a quick recap of what each lightweight starter provides and the key patterns to keep in mind:

| Database | Starter | Autoconfigured Bean | Reactive Pattern |
|---|---|---|---|
| Cassandra | `spring-boot-starter-cassandra` | `CqlSession` | `executeReactive()` → `Mono.from(ReactiveResultSet)` |
| Couchbase | `spring-boot-starter-couchbase` | `Cluster` | `collection.reactive()` → `ReactiveCollection` |
| Elasticsearch | `spring-boot-starter-data-elasticsearch` ⚠️ | `ReactiveElasticsearchClient` | Spring Data wrapper (no official reactive SDK) |
| MongoDB | `spring-boot-starter-mongodb` | `MongoClient` (reactive streams) | `Mono.from(...)` / `Flux.from(...)` |
| Neo4j | `spring-boot-starter-neo4j` | `Driver` | `ReactiveSession` + `Mono.usingWhen(...)` |
| Redis | _(no dedicated client starter)_ | via `spring-boot-starter-data-redis-reactive` | `ReactiveRedisTemplate` / Lettuce reactive commands |

> [!TIP]
> Across all stores, `StepVerifier` from the `reactor-test` module is your best friend for asserting reactive sequences in integration tests. Pair it with `@ServiceConnection`-backed Testcontainers for a hermetic, repeatable test environment without any manual port configuration.

The most important takeaway is knowing **when not to use** these raw clients: once your application needs query composition, pagination, auditing, or object mapping, reach for the full Spring Data repository abstraction instead. The native driver approach shines for targeted, performance-sensitive operations where you want maximum control with minimum overhead.

