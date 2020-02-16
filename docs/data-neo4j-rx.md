# Reactive programming with Neo4j   



Generate a Spring Boot project using [Spring Initializr](https://start.spring.io), and make sure the following options are chosen.

* Project type: Maven
* Java version : 11
* Spring Boot : 2.2.4.RELEASE
* Dependencies: Reactive Web, Lombok

> Do not add  Spring Data Neo4j  into the dependencies, we will use the new Spring Data Neo4j Rx instead.

Do not forget to add Lombok to your project dependencies.  We'll  use Lombok to get the Java codes clean as possible, esp. to generate the getters and setters, hashCode, equals, toString for you at compile time.

Download the generated project skeleton archive and extract the files into your machine.  

Open the *pom.xml* file in the project root folder, add the [Spring Data Neo4j RX](https://github.com/neo4j/sdn-rx/)  dependency from  the Neo4j  team manually. 

```xml
<dependency>
    <groupId>org.neo4j.springframework.data</groupId>
    <artifactId>spring-data-neo4j-rx-spring-boot-starter</artifactId>
    <version>${spring-data-neo4j-rx.version}</version>
</dependency>
```

Declare the property `spring-data-neo4j-rx.version` in properties. 

```xml
<spring-data-neo4j-rx.version>1.0.0-beta03</spring-data-neo4j-rx.version>
```

Now  let's add some codes to experience the new  Reactive features in  Spring Data Neo4j Rx.

Create a POJO class for presenting a node in Neo4j graph. 

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

    @CreatedDate
    private LocalDateTime createdDate;
}
```

In the above codes,  `@Node`, `@Id`, `@GeneratedValue` are located in the package `org.neo4j.springframework.data.core.schema`  which is part of the new Spring Data Neo4j Rx project.

Spring Data Neo4j Rx also supports the annotations from Spring Data Commons project. For example, you can use `Id` and `Persistent` from Spring Data Commons instead of the `Id` and  `Node` annotations from the new Spring Data  Neo4j RX.

Spring Data Neo4j Rx supports data auditing as well, so you can use `@CreatedDate`, `@CreatedBy`, `@LastModifiedDate`, `@LastModifiedBy` as usual, just add `@EnableNeo4jAuditing` on the application class to enable it. Unfortunately, there is no `AuditorAware` reactive variant available  in SDN RX, thus it can not fill `CreatedBy` and `lastModifiedBy` automatically.

Create a `Repository` class for `Post`.

```java
interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {
}
```

Create a `Controller` to perform CRUD operations.

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

The above codes are easy to understand, the main difference is here the methods return a Reactor's `Flux` or `Mono` type instead.  

Similar with WebMVC stack , to handle exception, you can define a `@RestControllerAdvice` bean and use `ExceptionHandler` method to handle specifial exceptions.

For example, in the `get` method, if the  Post is not found, throw an exception `PostNotFoundException` in the reactive flow by `Mono.error`.

```java
class PostNotFoundException extends RuntimeException {

    PostNotFoundException(Long id) {
        super("Post #" + id + " was not found");
    }
}
```

When the `PostNotFoundException` is caught, send a `404 Not Found` status code to the client.

```java
@RestControllerAdvice
@Slf4j
class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    ResponseEntity postNotFound(PostNotFoundException ex) {
        log.debug("handling exception::" + ex);
        return ResponseEntity.notFound().build();
    }

}
```

Almost done.  Let's try to initialize some data for demo purpose.

Create a `CommandLineRunner` bean to insert some data.

```java
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements CommandLineRunner {

    private final PostRepository posts;

    private final ReactiveNeo4jClient client;

    @Override
    public void run(String[] args) {
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
                .then()
                .doOnNext(
                        (v) -> client
                                .query("MATCH (p:Post) RETURN p")
                                .fetchAs(Post.class)
                                .mappedBy((t, r) -> (Post) (r.get("p").asObject()))
                                .all()
                                .subscribe(System.out::println)
                )
                .subscribe(
                        null,
                        null,
                        () -> log.info("done initialization...")
                );

    }

}
```

Benefit from the Spring Boot auto-configuration mechanism, a `ReactiveNeo4jClient` bean is ready for use.  You can use  it to interact with the Neo4j low level APIs, such as executing [Cypher](https://neo4j.com/developer/cypher-query-language/) queries.

To experience the  new reactive features, you have to use Neo4j 4.0 which is still under development.

Configure the Neo4j driver properties in *application.properties* file.

```pro
org.neo4j.driver.uri=bolt://localhost:7687
org.neo4j.driver.authentication.username=neo4j
org.neo4j.driver.authentication.password=test
```
Before running this application, it requires a running Neo4j server. The following code fragment is a docker-compose configuration which allow you run Neo4j server in docker containers.

```yaml
  neo4j:
    image: neo4j:4
    environment:
      - "NEO4J_AUTH=neo4j/test" # user none as value to disable auth setting
 #     - "NEO4J_ACCEPT_LICENSE_AGREEMENT=yes" # only neede by enterprise version
    ports:
      - 7687:7687 
      - 7474:7474 
```

Run the following command to start a local Neo4j server in Docker container.

```bash
docker-compose up neo4j
```

Start up our  application, when it is ready,  use `curl` to  test the APIs.

```bash
# Get all posts
curl http://localhost:8080/posts
[{"id":0,"title":"Post one","content":"content of Post one","createdDate":null},{"id":1,"title":"Post two","content":"content of Post two","createdDate":null}]


# Get post by id
curl http://localhost:8080/posts/0
{"id":0,"title":"Post one","content":"content of Post one","createdDate":null}

# Get none exsting post
curl -v http://localhost:8080/posts/10
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /posts/10 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Accept: */*
>
< HTTP/1.1 404 Not Found
< content-length: 0
<
* Connection #0 to host localhost left intact
```
Like Spring Data Mongo, SDN Rx also provides a `@DataNeo4jTest` annotation to provide  test slice capability.

Add the following dependency into the project pom.xml.

```xml
<dependency>
    <groupId>org.neo4j.springframework.data</groupId>
    <artifactId>spring-data-neo4j-rx-spring-boot-test-autoconfigure</artifactId>
    <version>${spring-data-neo4j-rx.version}</version>
    <scope>test</scope>
</dependency>
```
Create a test class to the SDN Rx features.

```java
@DataNeo4jTest
@Slf4j
public class PostRepositoryTest {

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
                .blockLast();// to make the tests work
//                .subscribe(
//                        (data) -> log.info("found post:" + data),
//                        (err) -> log.error("" + err),
//                        () -> log.info("done")
//                );
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
        //this.posts.deleteAll();
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
But you have to run a Neo4j server to serve this test, currently SDN Rx does not provide an embedded Neo4j for test scope.

Get the complete codes from  my [Github](https://github.com/hantsy/spring-reactive-sample).





