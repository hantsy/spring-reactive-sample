---
sort: 2
---

# Spring Data Neo4j

> The effort of [Spring Data Neo4j RX](https://github.com/neo4j/sdn-rx/) has been merged into the official Spring Data Neo4j project since Spring Data Neo4j 6.0. If you are using SDN Rx  it is better to upgrade to the official Spring Data Neo4j.

> This post targets Spring Data Neo4j 6.0 GA and Spring Boot 2.4.x release.

## Getting Started

Firstly, generate a Spring  WebFlux project skeleton using [Spring initializr](https://start.spring.io). 

* Choose Maven as project type(If you prefer Gradle, choose Gradle please)
* And select Spring Boot 2.4.3
* And select Java 11 or Java 15, personally I would like use the latest Java to experience the upcoming preview features
* Add the following dependencies.
  * Data Neo4j
  * Web Reactive
  * Lombok

Extract the downloaded archive into your disc, and import into your IDEs

Open the `pom.xml` file in the project root, you will see the following dependencies added in the *dependencies* section.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-neo4j</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Now , create a Noe4j entity class as the following.

```java
@Node
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String content;
}
```

A Neo4j entity is annotated with a `@Node`  annotation. 

> We used the `@Data`, `@ToString`, `@Builder` annotations provided in Lombok to erases the tedious methods, such as setters, getters, hashCode, equals, and toString in a POJO class.

Create a `Repository` for the  `Post` entity.

```java
interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {
}
```

Create a `RestController` to expose the simple CRUD RESTful APIs for  the `Post` entity.

```java
@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @GetMapping("")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
    public Mono<Post> create(@RequestBody Post post) {
        return this.posts.save(post);
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") Long id) {
        return Mono.just(id)
                .flatMap(posts::findById)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") Long id, @RequestBody Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(this.posts::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") Long id) {
        return this.posts.deleteById(id);
    }

}
```

In the above  `get` method, when the post is not found  it will throw a `PostNotFoundException`. Create a  `@RestControllerAdvice` annotated class to handle this exception.

```java
@RestControllerAdvice
@Slf4j
class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    ResponseEntity postNotFound(PostNotFoundException ex) {
        log.debug("handling exception::" + ex);
        return notFound().build();
    }

}
```

Add a `ReactiveTransactionManager` bean. In the Spring Data Neo4j 6.0, it seems activating a reactive transaction manager becomes a must, if it is not set, you will see  an exception thrown at the startup stage when running the application.

```java
// see: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.4.0-M2-Release-Notes#neo4j-1
@Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
public ReactiveTransactionManager reactiveTransactionManager(
    Driver driver,
    ReactiveDatabaseSelectionProvider databaseNameProvider) {
    return new ReactiveNeo4jTransactionManager(driver, databaseNameProvider);
}
```

> NOTE: If you are from SDN Rx,  adding a transaction manager is a must now.

Add a `CommandLineRunner` bean to initialize some sample data. Here we use `PostRepository` to insert two `Post` sample data.

```java
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;

    @Override
    public void run(String[] args) {
        log.info("start data initialization...");
        this.posts.deleteAll()
                .thenMany(
                        Flux
                                .just("Post one", "Post two")
                                .flatMap(
                                        title -> this.posts.save(Post.builder().title(title).content("The content of " + title).build())
                                )
                )
                .log()
                .thenMany(
                        this.posts.findAll()
                )
                .log("[Initializing data]")
                .subscribe(
                        data -> log.info("found post: {}", data),
                        err -> log.error("error", err),
                        () -> log.info("done")
                );

    }

}
```

Before starting this application, make sure there is a running Neo4j server.

There is a *docker-compose.yaml* file in the root folder of the [spring-reactive-sample](https://github.com/hantsy/spring-reactive-sample) repository which is prepared for bootstrapping dependent servers. 

Simply, run the following command to serve a Neo4j instance  in the Docker container. 

```bash
docker-compose up neo4j
```

And do not forget to configure the connection settings in the *application.properties*.

```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=test
```

>Note: if you are migrating from [SDN RX](https://github.com/neo4j/sdn-rx/), you need to replace all namespaces with the new `spring.neo4j`  prefix.

Now, you can run the application directly in IDEs, or using the following Maven command.

```bash
mvn spring-boot:run
```

After it run successfully, try to use `curl` command to verify the exposed APIs.

```bash
# curl http://localhost:8080/posts
[{"id":0,"title":"Post two","content":"The content of Post two","createdDate":"2020-11-04T10:35:14.1619567","updatedDate":"2020-11-04T10:35:14.1619567","createdBy":"hantsy","updatedBy":"hantsy"},{"id":1,"title":"Post one","content":"The content of Post one","createdDate":"2020-11-04T10:35:14.1481498","updatedDate":"2020-11-04T10:35:14.1481498","createdBy":"hantsy","updatedBy":"hantsy"}]
```

For the complete codes, check [spring-reactive-sample/boot-data-neo4j](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-neo4j).

## Customizing Queries

Like other Spring Data modules, Spring Data Neo4j also supports derived query methods.

For example, to find the posts by keyword that matches the title field, add the following method in the `PostRepository` interface.

```java
interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {

    Flux<Post> findByTitleLike(String title);
}
```

Add a new test method in `PostRespositoryTest` to verify it.

```java 
@Test
void testFindByTitle() {
    posts.findByTitleLike("one")
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
        .verifyComplete();
}
```

Run the test, it should work as expected.

Alternatively, you can use a `@Query` annotation to execute Cypher Query Language.

```java
import org.springframework.data.neo4j.repository.query.Query;
//...
    
interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {
    @Query("MATCH(post:Post) WHERE post.title =~ $title RETURN post")
    Flux<Post> findByTitleContains(String title);
    
    //
	...
}
```

Here we use a `regex` pattern in the where clause.

> More details about the syntax of  Cypher Query Language, please check the [official Neo4j documentation](https://neo4j.com/developer/cypher/).

Add a test method to verify it.

```java
@Test
void testFindByQuery() {
    posts.findByTitleContains("(?i).*" + "one" + ".*")
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
        .verifyComplete();
}
```

Here the `findByTitleContains` method has to accept a Regex pattern.

## ReactiveNeo4jClient

Once Spring Data Neo4j is configured in a reactive application, a `ReactvieNeo4jClient` bean is available in the Spring application context. 

Like the R2dbc's `DatabaseClient` , with `ReactiveNeo4jClient`, you can execute custom Cypher Queries and handle returning result freely. 

For example, to find all posts, it can be done by the following method.

```java
public Flux<Post> findAll() {
    var query = """
        MATCH (p:Post)
        RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
        """;
        return client
        .query(query)
        .fetchAs(Post.class).mappedBy((ts, r) ->
                                      Post.builder()
                                      .id(r.get("id").asLong())
                                      .title(r.get("title").asString())
                                      .content(r.get("content").asString())
                                      .createdAt(r.get("createdAt").asLocalDateTime(null))
                                      .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                      .build()
                                     )
        .all();
}
```

In the above codes. 

* The `query` use a multi-lined text block(available in the latest Java 15) to define a Cypher query.
* The  `client.query` to execute the defined query.
* The `fetchAs` to handle the returning result, similar to RowMapper in Jdbc/R2dbc to extract the result and wrap it into a POJO class.
* The `all` will return a `Flux` , if you want to return a single result, use `one` instead.

The following is an example of  basic CRUD operations.

```java
@Component
@RequiredArgsConstructor
public class PostRepository {

    private final ReactiveNeo4jClient client;

    public Mono<Long> count() {
        var query = """
                MATCH (p:Post) RETURN count(p)
                """;
        return client.query(query)
                .fetchAs(Long.class)
                .mappedBy((ts, r) -> r.get(0).asLong())
                .one();
    }

    public Flux<Post> findAll() {
        var query = """
                MATCH (p:Post)
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Flux<Post> findByTitleContains(String title) {
        var query = """
                MATCH (p:Post)
                WHERE p.title =~ $title
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .bind("(?!).*" + title + ".*").to("title")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Mono<Post> findById(Long id) {
        var query = """
                MATCH (p:Post)
                WHERE p.id = $id
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .bind(id).to("id")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Post> save(Post post) {
        var query = """
                MERGE (p:Post {id: $id})
                ON CREATE SET p.createdAt=localdatetime(), p.title=$title, p.content=$content
                ON MATCH SET p.updatedAt=localdatetime(), p.title=$title, p.content=$content
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;

        return client.query(query)
                .bind(post).with(data ->
                        Map.of(
                                "id", (data.getId() != null ? data.getId() : UUID.randomUUID().toString()),
                                "title", data.getTitle(),
                                "content", data.getContent()
                        )
                )
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Integer> deleteAll() {
        var query = """
                MATCH (m:Post) DETACH DELETE m
                """;
        return client.query(query)
                .run()
                .map(it -> it.counters().nodesDeleted());

    }

    public Mono<Integer> deleteById(Long id) {
        var query = """
                MATCH (p:Post) WHERE p.id = $id
                DETACH DELETE p
                """;
        return client
                .query(query)
                .bind(id).to("id")
                .run()
                .map(it -> it.counters().nodesDeleted());
    }
}

```

For the complete codes, check [spring-reactive-sample/boot-neo4j-cypher](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-neo4j-cypher).


## ReactiveNeo4jOperations

Like other Spring Data modules, Spring Data Neo4j provides a `ReactiveNeo4jOperations`(and  the implementation `ReactiveNeo4jTemplate`), it allows your to perform operations on Neo4j databases but by programmatic approaches.

Here is an example of `PostRepository`  which is reimplemented by `ReactiveNeo4jOperations`.

```java
@Component
@RequiredArgsConstructor
public class PostRepository  {
    private final ReactiveNeo4jOperations template;


    public Mono<Long> count() {
        return this.template.count(Post.class);
    }


    public Flux<Post> findAll() {
        return this.template.findAll(Post.class);
    }


    public Mono<Post> findById(Long id) {
        return this.template.findById(id, Post.class);
    }


    public Flux<Post> findByTitleContains(String title) {
        var postNode = node("Post").named("p");
        return this.template.findAll(
                match(postNode)
                        .where(postNode.property("title").contains(literalOf(title)))
                        .returning(postNode)
                        .build(),
                Post.class
        );
    }



    public Mono<Post> save(Post post) {
        return this.template.save(post);
    }


    public Flux<Post> saveAll(List<Post> data) {
        return this.template.saveAll(data);
    }


    public Mono<Void> deleteById(Long id) {
        return this.template.deleteById(id, Post.class);
    }


    public Mono<Void> deleteAll() {
        return this.template.deleteAll(Post.class);
    }
}

```

It it similar to the ReactiveNeo4jClient, but more simple.  Have a look at the `findAll`, the literal queries are replaced by Java Query Criteria APIs.

For the complete codes, check [spring-reactive-sample/boot-neo4j](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-neo4j).

## Data Auditing Support

Change the original `Post` entity, add the following fields to capture the timestamp and auditor when saving and updating the entity.

```java
@Node
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

	//...

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
```

In the above codes, there are some annotations applied on the fields. 

*  `CreatedDate` will fill the current date when saving the entity.
* `LastModifiedDate` will fill the current date when updating the entity.
* `CreatedBy` will retrieve the current auditor from  `ReactiveAuditorAware`  and fill it when saving the entity.
* `LastModifiedBy` will retrieve the current auditor from  `ReactiveAuditorAware`  and fill it when updating the entity.

The `CreatedDate` and `LastModifiedDate` can be applied on the traditional `java.util.Date`, the new Java 8 DateTime API and [Joda](https://www.joda.org) time types.

The class type of `CreatedBy` and `ModifiedBy` are dependent  on the parameterized type of the declaration of `ReactiveAuditorAware` bean.

Add a `ReactiveAuditorAware` bean to serve the auditor in the entity when saving and updating it.

```java
@Bean
public ReactiveAuditorAware<String> reactiveAuditorAware() {
    return () -> Mono.just("hantsy");
}
```

> Note : in the real world applications, you can retrieve the current user from current SecurityContextHolder.

By default Spring Boot do not autoconfigure the auditing feature. Do not forget to add a `@EnableReactiveNeo4jAuditing`  annotation on the `@Configuration` class to activate the data auditing feature.

```java
@Configuration(proxyBeanMethods = false)
@EnableReactiveNeo4jAuditing
class DataConfig {

    @Bean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {... }
}
```

Run the application, you will see the following logging info printed by the `DataInitializer` bean.

 ```bash
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] [Initializing data]                      : onNext(Post(id=2, title=Post one, content=The content of Post one, createdDate=2020-11-08T10:22:24.554356100, updatedDate=2020-11-08T10:22:24.554356100, createdBy=hantsy, updatedBy=hantsy))
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] com.example.demo.DataInitializer         : found post: Post(id=2, title=Post one, content=The content of Post one, createdDate=2020-11-08T10:22:24.554356100, updatedDate=2020-11-08T10:22:24.554356100, createdBy=hantsy, updatedBy=hantsy)
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] [Initializing data]                      : onNext(Post(id=3, title=Post two, content=The content of Post two, createdDate=2020-11-08T10:22:24.562356700, updatedDate=2020-11-08T10:22:24.562356700, createdBy=hantsy, updatedBy=hantsy))
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] com.example.demo.DataInitializer         : found post: Post(id=3, title=Post two, content=The content of Post two, createdDate=2020-11-08T10:22:24.562356700, updatedDate=2020-11-08T10:22:24.562356700, createdBy=hantsy, updatedBy=hantsy)
 ```

You can also verify it via `curl` .

```bash
> curl http://localhost:8080/posts
[{"id":2,"title":"Post one","content":"The content of Post one","createdDate":"2020-11-08T10:22:24.5543561","updatedDate":"2020-11-08T10:22:24.5543561","createdBy":"hantsy","updatedBy":"hantsy"},{"id":3,"title":"Post two","content":"The content of Post two","createdDate":"2020-11-08T10:22:24.5623567","updatedDate":"2020-11-08T10:22:24.5623567","createdBy":"hantsy","updatedBy":"hantsy"}]
```

For the complete codes, check [spring-reactive-sample/boot-data-neo4j](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-neo4j).


## Testing

Since version 1.4,  Spring Boot provided a new test harness so-called **test slice** to test features easier than previous version,  which included a series of  `AutoConfigureXXX` to allow developers to test desired features in an isolated environment. 

For example,  adding a test scoped H2 dependency into your project and annotating your test class with `@DataJpaTest`, you can test your `Repository` class against an embedded H2 instead of the real runtime database. With the `@DataJpaTest`, Spring test context only loads the essential configuration for testing JPA facilities, no need to load the all configuration for the whole application .  

For Spring Data Neo4j, Spring Boot also provides a `@DataNeo4jTest` for testing Neo4j facilities, but unfortunately it does not include an utility to start up an embedded Neoj4 database for your tests. There are some solutions to overcome this barrier.

* Neo4j provides a test harness which provides APIs to start and stop an embedded Neo4j server by programmatic approach.
*  [Testcontainers](https://www.testcontainers.org)  is a generic solution to run Docker containers for the testing framework, it is easy to start a Neo4j database in a Docker container when testing Spring Data Neo4j repositories.

### Test with Neo4j test harness

Add the following dependency into your *pom.xml*.

```xml
<dependency>
    <groupId>org.neo4j.test</groupId>
    <artifactId>neo4j-harness</artifactId>
    <version>${neo4j-harness.version}</version>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-nop</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

In your test, add the following codes to serve a running Neo4j server when running tests.

```java
@DataNeo4jTest
@Transactional(propagation = Propagation.NEVER)
@Slf4j
public class PostRepositoryWithNeo4jHarnessTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {

        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()//disable http server
                .build();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> null);
    }

    @AfterAll
    static void stopNeo4j() {
        embeddedDatabaseServer.close();
    }
    ...
}
```

In the above codes,

* We use the JUnit 5 lifecycle hooks, such as  `beforeAll` and `afterAll` to start and stop an embedded Neo4j server.
* Use a static method annotated with `@DynamicPropertySource` to bind the Neo4j properties to the Spring test context.

Now you can add tests as general.

### Test with Testcontainers

Testcontainers provides a simple programmatic API abstraction for you to bootstrap a Docker container in your testing codes.

Testcontainers is available in the official [Spring initializr](https://start.spring.io). You can add *TestContainers* as dependencies when generating new project using [Spring initializr](https://start.spring.io).

Or add the following dependencies into your *pom.xml* manually.

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>neo4j</artifactId>
    <scope>test</scope>
</dependency>
```

And import the testcontainers BOM in the `depedencyManagement`section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>${testcontainers.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

In the above code, 

* The `junit-jupiter` artifact is used to integrate *TestContainers* with JUnit 5 platform.
* The `neo4j` artifact provides APIs to compose a Neo4j Docker container.

Create a test for `PostRepository`.

```java
@SpringBootTest
//@DataNeo4jTest
@Testcontainers
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PostRepositoryWithTestContainersTest {

    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.0")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }


    @Autowired
    private PostRepository posts;

    @BeforeEach
    public void setup() throws IOException {
        log.debug("running setup.....,");
        this.posts.deleteAll()
                .thenMany(testSaveMethod())
                .log()
                .thenMany(testFoundMethod())
                .log()
                .blockLast(Duration.ofSeconds(5));// to make the tests work
    }

    private Flux<Post> testSaveMethod() {
        var data = Stream.of("Post one", "Post two")
                .map(title -> Post.builder().title(title).content("The content of " + title).build())
                .collect(Collectors.toList());
        return Flux.fromIterable(data)
                .flatMap(it -> this.posts.save(it));
    }

    private Flux<Post> testFoundMethod() {
        return this.posts
                .findAll(Example.of(Post.builder().title("Post one").build()));
    }

    @AfterEach
    void teardown() {
    }

    @Test
    void testAllPosts() {
        posts.findAll().sort(Comparator.comparing(post -> post.getTitle()))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }

}
```

In the above codes, 

*  A test class is annotated with a general `@SpringBootTest` annotation(will load all configurations) or a `@DataNeo4jTest` annotation. When using `@DataNeo4jTest`, you have to add an extra `@Transactional(propagation = Propagation.NEVER)`, check  [spring-boot issue#23630](https://github.com/spring-projects/spring-boot/issues/23630) for more details.
* A `@Testcontainers` is added on the class level, thus the Testcontainers facilities will contribute the test lifecycle.
* A static `@Container` resource is defined, it will be initialized before the test execution.
*  By default, JUnit 5 uses a `PER_METHOD` strategy to bootstrap a test, if you set a global `PER_CLASS` strategy in the *junit-platform.properties*, add a `@TestInstance(TestInstance.Lifecycle.PER_METHOD)` to override it.
* A static method annotated with `@DynamicPropertySource` is used to bind properties from the running Docker container to the Spring environmental variables before the test is running. 
* You can inject your `Repository` beans, and the Neo4j specific `ReactiveNeo4jOperations`, `ReactiveNeo4jClient`, `Driver`  beans etc. in a  `@DataNeo4jTest` annotated test directly.
* Generally, you can add `@BeforeEach`, `@AfterEach` methods to hook the JUnit test lifecycle.
* In the `@Test` method, we usually utilizes reactor's `StepVerifier` to assert the result.

> In the original [SDN Rx]((https://github.com/neo4j/sdn-rx/)  ), it provided a `@ReactiveDataNeo4jTest` for testing reactive applications, this annotation is not available in Spring Boot 2.4.

Alternatively, you can create a `ApplicationContextInitializer` to start a Neo4j Docker container manually.

```java
@DataNeo4jTest
@Transactional(propagation = Propagation.NEVER)
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@Slf4j
public class PostRepositoryTest {


    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.0").withoutAuthentication();
            neo4jContainer.start();
            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> neo4jContainer.stop());
            TestPropertyValues
                    .of(
                            "spring.neo4j.uri=" + neo4jContainer.getBoltUrl(),
                            "spring.neo4j.authentication.username=neo4j",
                            "spring.neo4j.authentication.password=" + neo4jContainer.getAdminPassword()
                    )
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

...
}
```

In the above, we use a `ContextConfiguration` to apply the context initializers. In the real world application, you can extract `TestContainerInitializer` to a standalone class, and thus it is easy to reuse in any tests that requires a running Neo4j server instance.

For the complete codes, check [spring-reactive-sample/boot-data-neo4j](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-neo4j).



