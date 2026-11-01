# Accessing MongoDB with Spring Data Mongo

MongoDB is one of the most natural fits for a reactive stack. It stores documents in a JSON-like shape, and its Java driver is built around streaming operations that align well with Reactor and WebFlux.

In Spring, you typically work with MongoDB in one of two ways:

- through Spring Data MongoDB repositories and templates
- directly with the reactive MongoDB driver via `MongoClient` when you need more control over the data flow

The example below keeps the focus on the real-world flow: a small reactive API, a document model, and a straightforward repository setup that you can adapt to a real service.

## Getting started

The quickest way to start is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../start/intro.md) chapter. Select **Spring WebFlux** and **Spring Data MongoDB**.

If you are adding MongoDB to an existing project, include the following dependencies in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

Lombok is optional, but it helps remove repetitive boilerplate such as getters, setters, `equals`, `hashCode`, and `toString`.

If you are building a plain Spring project without Boot auto-configuration, use the following dependencies instead:

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-reactivestreams</artifactId>
</dependency>
```

In that case, you also need a small configuration class to enable reactive MongoDB support:

```java
@EnableReactiveMongoRepositories(basePackageClasses = {MongoConfig.class})
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${mongo.uri}")
    String mongoUri;

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        return "blog";
    }
}
```

You can explore this setup in [data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo).

With Spring Boot, the connection is usually configured automatically from `application.properties` or `application.yml`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/blog
```

## Defining the document model

Create a `Post` document class that maps to a MongoDB collection:

```java
@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    private String id;
    private String title;
    private String content;
}
```

The annotations in this example are straightforward:

1. `@Document` marks the class as a MongoDB document.
2. `@Id` identifies the document ID field.
3. `@Data` is from `Lombok` and help to generates getters, setters, `equals`, `hashCode` methods at compile time.
4. `@ToString` generates a `toString()` implementation.
5. `@Builder` enables the builder pattern.
6. `@NoArgsConstructor` creates a no-args constructor.
7. `@AllArgsConstructor` creates a constructor with all fields.

## Creating a repository

Define a repository interface by extending `ReactiveMongoRepository`:

```java
interface PostRepository extends ReactiveMongoRepository<Post, String> {
}
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

    @GetMapping
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping
    public Mono<Post> create(@RequestBody Post post) {
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

## Seeding sample data

You can use a `CommandLineRunner` to insert a few sample records when the application starts:

```java
@Component
@Slf4j
class DataInitializr implements CommandLineRunner {

    private final PostRepository posts;

    public DataInitializr(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String[] args) {
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
}
```

`CommandLineRunner` ensures the initialization logic runs once the application has started.

## Running the application

Before starting the app, make sure MongoDB is running locally.

!!! note

    If MongoDB is not installed yet, visit the [MongoDB download page](https://www.mongodb.com/download-center) and install it first.

If you prefer Docker, it is easy to start MongoDB with Docker Compose:

```yml
version: "3.3"

services:
  mongodb:
    image: mongo
    volumes:
      - mongodata:/data/db
    ports:
      - "27017:27017"
    command: --smallfiles --rest
    # command: --smallfiles --rest --auth

volumes:
  mongodata:
```

!!! note
    The repository also includes a Docker Compose file that can start the database services for you.

Start MongoDB in a container:

```bash
docker-compose up mongodb
```

Then launch the application:

```bash
mvn spring-boot:run
```

You can test the API with `curl`:

```bash
curl -v http://localhost:8080/posts
```

Example response:

```text
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.1
> Accept: */*
>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json;charset=UTF-8
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
<
[{"id":"599149d53c44062e08c58b86","title":"Post one","content":"content of Post one","createdDate":[2017,8,14,14,57,25,71000000]},{"id":"599149d53c44062e08c58b87","title":"Post two","content":"content of Post two","createdDate":[2017,8,14,14,57,25,173000000]}]
* Connection #0 to host localhost left intact
```

## Customizing queries

Like other Spring Data projects, Spring Data MongoDB supports query derivation in repositories.

For example:

```java
interface PostRepository extends ReactiveMongoRepository<Post, String> {

    Flux<Post> findByTitleContains(String title);
}
```

You can also add a custom `@Query` to run a raw MongoDB query directly. The following example uses Java text blocks (available since Java 13):

```java
@Query(
    value = """
    {
        "title" : {
            "$regularExpression" : { "pattern" : ?0, "options" : "" }
        }
    }
    """,
    sort = """
    {
        "title" : 1,
        "createdDate" : -1
    }
    """
)
Flux<Post> findByKeyword(String q);
```

Explore the example codes in [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo).

## Pagination

Most reactive repositories support `Pageable` as a method parameter, including MongoDB repositories.

```java
interface PostRepository extends ReactiveMongoRepository<Post, String> {

    Flux<PostSummary> findByTitleContains(String title, Pageable page);
}
```

Note that there is no `findAll(Pageable page)` method in the reactive repository API.

If you need a pageable-style operation across all records, you can do it manually:

```java
var all = posts.findAll()
    .take(...)
    .skip(...);
```

All pageable queries return a `Flux` result.

To count matching records, you can issue a separate count query:

```java
Mono<Long> countByTitleContains(String title);
```

!!! note

    Reactive Spring Data APIs do not return a `Page` object.

Explore the pagination sample in [data-mongo-pageable](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-pageable).

## `ReactiveMongoTemplate`

`ReactiveMongoTemplate` offers a more programmatic and fluent way to execute MongoDB queries.

The next example rewrites the repository logic using `ReactiveMongoTemplate` directly:

```java
@Component
@RequiredArgsConstructor
class PostRepository {

    private final ReactiveMongoTemplate template;

    Flux<Post> findByKeyword(String q) {
        var reg = ".*" + q + ".*";
        return template.find(
            query(where("title").regex(reg).orOperator(where("content").regex(reg))),
            Post.class
        );
    }

    Flux<Post> findByTitleContains(String title) {
        var reg = ".*" + title + ".*";
        return template.find(query(where("title").regex(reg)), Post.class);
    }

    Flux<Post> findByTitleContains(String title, Pageable page) {
        var reg = ".*" + title + ".*";
        return template.find(query(where("title").regex(reg)).with(page), Post.class);
    }

    public Flux<Post> findAll() {
        return template.findAll(Post.class);
    }

    public Mono<Post> save(Post post) {
        return template.save(post);
    }

    public Flux<Post> saveAll(List<Post> data) {
        return Flux.fromIterable(data).flatMap(template::save);
    }

    public Mono<Post> findById(String id) {
        return template.findById(id, Post.class);
    }

    public Mono<Long> deleteById(String id) {
        return template.remove(query(where("id").is(id)), Post.class)
            .map(DeleteResult::getDeletedCount);
    }

    public Mono<Long> deleteAll() {
        return template.remove(Post.class).all().map(DeleteResult::getDeletedCount);
    }
}
```

You can explore this approach in [boot-data-mongo-template](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-template).

## Data auditing support

Spring Data MongoDB supports auditing in a way similar to Spring Data JPA. It can automatically populate the current user and created/last-modified timestamps.

Add `@EnableReactiveMongoAuditing` to your application class to enable auditing:

```java
@EnableReactiveMongoAuditing
public class DemoApplication {}
```

Then add a field such as `createdDate` and annotate it with `@CreatedDate`:

```java
@CreatedDate
private LocalDateTime createdDate;
```

This value is automatically set when a document is inserted.

To provide the current auditor, create a `ReactiveAuditorAware` bean:

```java
@Bean
ReactiveAuditorAware<String> auditorAware() {
    return () -> Mono.just("hantsy");
}
```

You can also mark who created and last updated the entity with `@CreatedBy` and `@LastModifiedBy`:

```java
class Post {
    //...
    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
    //...
}
```

Explore the auditing example in [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo).

## QueryDSL

QueryDSL is a type-safe query builder for Java that allows you to construct queries in a fluent and type-safe manner. It supports Java Collections, JPA and MongoDB, etc.

Spring Data MongoDB also supports QueryDSL-style reactive queries.

Add the following dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <scope>provided</scope>
</dependency>
```

Then configure the APT plugin to generate the QueryDSL metadata:

```xml
<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <outputDirectory>target/generated-sources/java</outputDirectory>
                <!--<processor>com.querydsl.mongodb.morphia.MorphiaAnnotationProcessor</processor>-->
                <processor>lombok.launch.AnnotationProcessorHider$AnnotationProcessor,org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor</processor>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Modify the repository to include the QueryDSL executor:

```java
interface PostRepository extends ReactiveMongoRepository<Post, String>, ReactiveQuerydslPredicateExecutor<Post> {
}
```

Use it like this:

```java
this.postRepository.findAll(QPost.post.title.containsIgnoreCase("my"));
```

See the usage example in [boot-data-mongo-querydsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-querydsl).

## Tailable queries

A MongoDB _tailable cursor_ is a special kind of query that behaves a bit like a message queue. It keeps a cursor open and emits new documents as they are inserted.

This is useful when you want to consume a stream of events in a reactive application.

```java
@Tailable
Flux<Message> readByAll();
```

The resulting stream can be subscribed by an SSE endpoint, a WebSocket endpoint, or an RSocket channel.

There is a simple SSE example in [spring-reactive-sample/boot-data-mongo-tailable](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-mongo-tailable).

For more complete examples, see these projects:

- [Angular and WebSocket sample](https://github.com/hantsy/angular-spring-websocket-sample)
- [Angular and Server-Sent Event sample](https://github.com/hantsy/angular-spring-sse-sample)
- [Angular and RSocket sample](https://github.com/hantsy/angular-spring-rsocket-sample)

## GridFS: file upload and download

A slightly more realistic use case is storing uploaded files in MongoDB with `ReactiveGridFsTemplate` and streaming them back to clients.

First, declare a `ReactiveGridFsTemplate` bean:

```java
@Bean
public ReactiveGridFsTemplate reactiveGridFsTemplate() throws Exception {
    return new ReactiveGridFsTemplate(reactiveMongoDbFactory(), mappingMongoConverter());
}
```

Then handle uploads and downloads in a controller:

```java
@RestController
@RequestMapping("/multipart")
@RequiredArgsConstructor
public class MultipartController {

    private final ReactiveGridFsTemplate gridFsTemplate;

    @PostMapping
    public Mono<ResponseEntity> upload(@RequestPart Mono<FilePart> fileParts) {
        return fileParts
            .flatMap(part -> this.gridFsTemplate.store(part.content(), part.filename()))
            .map(id -> ok().body(Map.of("id", id.toHexString())));
    }

    @GetMapping("{id}")
    public Flux<Void> read(@PathVariable String id, ServerWebExchange exchange) {
        return this.gridFsTemplate.findOne(query(where("_id").is(id)))
            .log()
            .flatMap(gridFsTemplate::getResource)
            .flatMapMany(r -> exchange.getResponse().writeWith(r.getDownloadStream()));
    }
}
```

In this example, the `upload` method stores the uploaded `FilePart` content in GridFS and returns the generated file ID. The `read` method looks up the file by ID and writes it to the HTTP response.

Finally, build the multipart request on the client side.

`MultipartBodyBuilder` assembles multipart bodies for the client. The following test uploads a file and reads it back:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class DemoApplicationTests {

    @LocalServerPort
    private int port;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + this.port)
            .build();
    }

    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileParts", new ClassPathResource("/foo.txt", DemoApplicationTests.class));
        return builder.build();
    }

    @Test
    public void testUpload() throws IOException {
        byte[] result = client
            .post()
            .uri("/multipart")
            .bodyValue(generateBody())
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult().getResponseBody();

        ObjectMapper objectMapper = new ObjectMapper();
        Map bodyMap = objectMapper.readValue(result, Map.class);

        String fileId = (String) bodyMap.get("id");
        log.debug("updated file id: " + fileId);
        assertNotNull(fileId);

        client
            .get()
            .uri("/multipart/{id}", fileId)
            .exchange()
            .expectStatus().isOk();
    }
}
```

You can explore the GridFS example in [multipart-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart-data-mongo) and [boot-data-mongo-gridfs](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-gridfs).

## Using `MongoClient`

Spring Boot's `spring-boot-starter-mongodb` registers a reactive `com.mongodb.reactivestreams.client.MongoClient` bean in a reactive application, without requiring Spring Data repositories.

The MongoDB driver provides both synchronous and reactive variants. The reactive driver exposes publishers, which can be converted into Reactor types with `Mono.from(...)` and `Flux.from(...)`.

!!! tip

    The starter transitively includes `mongodb-driver-sync`, even when you only need the reactive driver. For a leaner classpath, exclude the sync driver explicitly and declare `mongodb-driver-reactivestreams` directly:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
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

## Implementing a repository with `MongoClient`

Reactive collections return standard Reactive Streams publishers. You can bridge these to Reactor using `Mono.from(...)` or `Flux.from(...)`:

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

## Testing with Testcontainers

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

Explore [boot-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mongo) to see how `Mono.from(...)` bridges raw driver publishers into Reactor, while `StepVerifier` asserts document insertion and retrieval without a single blocking call.
