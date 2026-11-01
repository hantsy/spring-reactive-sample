# Accessing Couchbase with Spring Data Couchbase

Couchbase blurs the line between document storage and a richer distributed data platform. It is especially useful when you want JSON documents, flexible indexing, and a lower-latency data layer without giving up the convenience of Spring abstractions.

In Spring, you usually work with Couchbase in one of two ways:

- through Spring Data Couchbase repositories and templates
- directly with the Java SDK's `Cluster` and `ReactiveCollection` when you need a more explicit control flow

The sample here focuses on the WebFlux-friendly path and demonstrates the patterns you would use in a real application without adding unnecessary ceremony.

## Getting started

The quickest way to start is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../start/intro.md) chapter. Select **Spring WebFlux** and **Spring Data Couchbase (Reactive)**.

If you are adding Couchbase to an existing project, include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-couchbase-reactive</artifactId>
</dependency>
```

If you are building a plain Spring project without Boot auto-configuration, use the following dependencies instead:

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

In that case, you also need a configuration class extending `AbstractCouchbaseConfiguration`, with `@EnableReactiveCouchbaseRepositories` to enable reactive `Repository` support:

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

You can explore this setup in [data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/data-couchbase).

With Spring Boot, the connection is usually configured automatically from `application.properties`:

```properties
spring.couchbase.connection-string=couchbase://localhost
spring.couchbase.username=Administrator
spring.couchbase.password=password
spring.data.couchbase.bucket-name=default
```

## Defining the document model

Create a `Post` class that maps to a Couchbase document:

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

The annotations in this example are straightforward:

1. `@Document` marks the class as a Couchbase document.
2. `@Id` designates the document key; `@GeneratedValue(strategy = GenerationType.UNIQUE)` lets Spring Data generate a unique key for you.
3. `@Field` maps a differently named field in the document.

## Creating a repository

Define a repository interface by extending `ReactiveCouchbaseRepository`:

```java
@ViewIndexed(designDoc = "post", viewName = "all")
interface PostRepository extends ReactiveCouchbaseRepository<Post, String> {}
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

Before starting the app, make sure Couchbase is running locally.

If you prefer Docker, start Couchbase with the Docker Compose file in the repository root:

```bash
docker-compose up couchbase
```

Then open http://localhost:8091 to create the administrator user account and bucket, and launch the application:

```bash
mvn spring-boot:run
```

You can test the API with `curl`:

```bash
curl -v http://localhost:8080/posts
```

## Working with the native client

The `spring-boot-starter-couchbase` starter auto-configures a Couchbase `Cluster` bean. By obtaining the collection reference, you can perform document operations directly — call `.reactive()` on the collection to obtain a `ReactiveCollection`; the SDK exposes it natively, so no additional reactive driver is needed.

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

### Implementing a repository with `ReactiveCollection`

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

## Testing with Testcontainers

Define a `CouchbaseContainer` bean with Spring Boot's `@ServiceConnection` — the bucket and credentials are provisioned for the ephemeral server:

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

You can explore the native-client example in [boot-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-couchbase), and the repository-based one in [boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-couchbase).
