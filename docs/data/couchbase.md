# Accessing Couchbase with Spring Data Couchbase

Couchbase is a distributed JSON document database. Spring Data Couchbase adds reactive support on top of the Couchbase Java SDK 3.x — a `ReactiveCouchbaseTemplate` and `ReactiveCouchbaseRepository` — and the SDK itself exposes a `ReactiveCollection` for the thin-client path.

This page follows the flow used across the chapter: generate a project as in [Getting Started](../start/intro.md), add the dependency, try the native client, then move to the Spring Data abstractions.

## Getting started

Generate a project skeleton from [Spring Initializr](https://start.spring.io) as described in the [Getting Started](../start/intro.md) chapter — choose **Spring WebFlux** and **Spring Data Couchbase (Reactive)** — or add the starter to an existing `pom.xml`:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-couchbase-reactive</artifactId>
</dependency>
```

For a freestyle Spring project (no Spring Boot auto-configuration), use the plain dependencies instead:

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-couchbase</artifactId>
</dependency>
<dependency>
	<groupId>com.couchbase.client</groupId>
	<artifactId>java-client</artifactId>
</dependency>
```

Start a Couchbase server with the `docker-compose.yml` in the repository root:

```bash
docker-compose up couchbase
```

## Working with the native client

The `spring-boot-starter-couchbase` starter auto-configures a Couchbase `Cluster` bean. By obtaining the collection reference, you can perform document operations directly. For reactive operations, call `.reactive()` on the collection to obtain a `ReactiveCollection` — the Couchbase Java SDK exposes it natively, so no additional reactive driver is needed.

### Project setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-couchbase</artifactId>
</dependency>
```

### Implementing a thin repository

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

## Spring Data Couchbase

### Spring configuration

Create a `@Configuration` class extending `AbstractCouchbaseConfiguration`, and add `@EnableReactiveCouchbaseRepositories` to enable reactive `Repository` support:

```java
@Configuration
@EnableReactiveCouchbaseRepositories(basePackageClasses = {CouchbaseConfig.class})
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("${couchbase.user}")
    private String user;

    @Value("${couchbase.password}")
    private String password;

    @Value("${couchbase.connection-string}")
    private String connectionString;

    @Value("${couchbase.bucket-name}")
    private String bucket;

    @Override
    public String getConnectionString() {
        return this.connectionString;
    }

    @Override
    public String getUserName() {
        return this.user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getBucketName() {
        return this.bucket;
    }
}
```

### Entity and repository

Create a `Post` class to represent a document in Couchbase, and add a `@Document` annotation at the class level:

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UNIQUE)
    private String id;
    private String title;

    @Field("content")
    private String content;
}
```

- `@Document` marks the class as a Couchbase document.
- `@Id` designates the document key; `@GeneratedValue(strategy = GenerationType.UNIQUE)` lets Spring Data generate a unique key for you.
- `@Field` maps a differently named field in the document.

Create the `PostRepository` interface:

```java
@ViewIndexed(designDoc = "post", viewName = "all")
interface PostRepository extends ReactiveCouchbaseRepository<Post, String> {}
```

### Run against a Couchbase server

Start a Couchbase server first:

```bash
docker-compose up couchbase
```

Open http://localhost:8091 to create the administrator user account and bucket. Set the connection properties in `application.properties`:

```properties
couchbase.user=Administrator
couchbase.password=password
couchbase.connection-string=couchbase://localhost
couchbase.bucket-name=default
```

### Boot starter

For Spring Boot applications, add `spring-boot-starter-data-couchbase-reactive` to the project dependencies and configure the connection in `application.properties`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-couchbase-reactive</artifactId>
</dependency>
```

```properties
spring.couchbase.connection-string=couchbase://localhost
spring.couchbase.username=Administrator
spring.couchbase.password=password
spring.data.couchbase.bucket-name=default
```

No extra configuration class is needed — Spring Boot auto-configures Couchbase and registers all essential beans. The [boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-couchbase) sample uses a `WebFlux.fn` handler layer on top of the repository.

### Seeding sample data

Add sample data via a `CommandLineRunner` bean or an `ApplicationRunner` bean:

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

## All samples

| Sample | Description |
| :--- | :--- |
| [data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/data-couchbase) | Freestyle Spring configuration with `ReactiveCouchbaseRepository` |
| [boot-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-couchbase) | Native `Cluster` (thin client) with `ReactiveCollection` |
| [boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-couchbase) | Spring Boot starter with `ReactiveCouchbaseRepository` |
