# Update: Accessing Neo4j with Spring Boot 2.4

In Spring Boot 2.3, when you want to use reactive stack with Neo4j  database - the well-known Graph NoSQL database, you should have to use [the work](https://github.com/neo4j/sdn-rx/) from Neo4j team.

I have written [a post](https://medium.com/@hantsy/reactive-programming-with-neo4j-fb926a423d33) before to describe this project. 

The effort of  [Spring Data Neo4j RX](https://github.com/neo4j/sdn-rx/)  has been merged into the official Spring Data Neo4j project, and we will have new updated reactive support in the final Spring Data Neo4j 6.0 and the upcoming Spring Boot 2.4.

In this post, will recreate our former example application using the newest Spring Boot 2.4, it will also include the points  which are useful for those are migrating from [Spring Data Neo4j RX](https://github.com/neo4j/sdn-rx/) .

Firstly, open the http://start.spring.io page in your favorite browser, and create a Spring  WebFlux project using [Spring initializr](https://start.spring.io). 

* Choose Maven as project type(If you prefer Gradle, choose Gradle please)
* And select Spring Boot 2.4.0-RC1
* And select Java 11 or Java 15 
* Add the following dependencies.
  * Data Neo4j
  * Web Reactive
  * Lombok

Extract the downloaded archive into your disc, and import into your IDE, such as IntelliJ IDEA.

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

Create an Noe4j entity class.

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

Let's have a look at this controller, it is very similar to the general imperative version in Spring MVC, but here it returns a reactive specific `Mono` or `Flux` in these methods.

In the above  `get` method, when the post by id is not found  it will throw a `PostNotFoundException`. Create a  `@RestControllerAdvice` annotated class to handle this exception.

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

Add a `ReactiveTransactionManager` bean. In the Spring Data Neo4j 6.0, it seems activate a reactive transaction manager become a must, if this is not set, it will throw exceptions at the startup stage when running the application.

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

Use  a  `CommandLineRunner` bean to initialize some sample data. Here we use `PostRepository` to insert two `Post` sample data.

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

To run this application, a running Neo4j server should be provided.

There is a *docker-compose.yaml* file prepared in the root folder of the [spring-reactive-sample](https://github.com/hantsy/spring-reactive-sample) repository. Simply, run the following command to serve a Neo4j instance  in the Docker container. 

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

Grab the [source code]() from my github.

