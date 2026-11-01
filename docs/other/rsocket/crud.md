
# Building a CRUD application with RSocket

The [TCP](tcp.md) and [WebSocket](websocket.md) examples use routes to exchange simple greetings. The [rsocket-crud example](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-crud) demonstrates a more realistic application backed by Spring Data R2DBC, with an RSocket server exposing persistence routes and a WebFlux client calling them with `RSocketRequester`.

## The server application

Generate a project from [Spring Initializr](https://start.spring.io) with **RSocket**, **Spring Data R2DBC**, **H2 Database**, and **Lombok**, or add the dependencies directly:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-rsocket</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

The server exposes the RSocket API over TCP:

```properties
spring.rsocket.server.port=7000
spring.rsocket.server.transport=tcp
spring.r2dbc.url=r2dbc:h2:mem:///blogdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.r2dbc.username=sa
spring.r2dbc.password=
```

If you are new to Spring Data R2DBC, start with [Accessing RDBMS with Spring R2DBC and Spring Data R2DBC](../../data/r2dbc.md).

### Defining the entity and repository

Create a simple POJO:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
class Post {

    @Id
    @Column("id")
    private Integer id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;
}
```

Then define a repository for `Post`:

```java
interface PostRepository extends R2dbcRepository<Post, Integer> {
}
```

### Serving CRUD messaging routes

Create a `Controller` class to handle the incoming messages. Each route follows a consistent naming pattern: `posts.` plus the action, with `.{id}` carrying the path variable:

```java
@Controller
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @MessageMapping("posts.findAll")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @MessageMapping("posts.findById.{id}")
    public Mono<Post> get(@DestinationVariable("id") Integer id) {
        return this.posts.findById(id);
    }

    @MessageMapping("posts.save")
    public Mono<Post> create(@Payload Post post) {
        return this.posts.save(post);
    }

    @MessageMapping("posts.update.{id}")
    public Mono<Post> update(@DestinationVariable("id") Integer id, @Payload Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());
                    return p;
                })
                .flatMap(p -> this.posts.save(p));
    }

    @MessageMapping("posts.deleteById.{id}")
    public Mono<Void> delete(@DestinationVariable("id") Integer id) {
        return this.posts.deleteById(id);
    }
}
```

As a bonus, add a `@Query` method to the repository and a route that filters posts by keyword:

```java
interface PostRepository extends R2dbcRepository<Post, Integer> {

    @Query("SELECT * FROM posts WHERE title like $1")
    Flux<Post> findByTitleContains(String name);
}
```

```java
@MessageMapping("posts.titleContains")
public Flux<Post> titleContains(@Payload String title) {
    return this.posts.findByTitleContains(title);
}
```

### Initializing the data

Create a `schema.sql` and a `data.sql` file to create the table and seed the records:

```sql
-- schema.sql
CREATE TABLE IF NOT EXISTS posts (id SERIAL PRIMARY KEY, title VARCHAR(255), content VARCHAR(255));
```

```sql
-- data.sql
DELETE FROM posts;
INSERT INTO posts (title, content) VALUES ('post one in data.sql', 'content of post one in data.sql');
```

!!! note

    In the early Boot 2.3 milestones, the `ConnectionFactoryInitializer` auto-configuration had not been ported yet, so the samples declared the bean by hand. Modern Spring Boot initializes `schema.sql` and `data.sql` for R2DBC automatically; the explicit `ConnectionFactoryInitializer` bean is only shown here for completeness.

Start the server:

```bash
mvn spring-boot:run
```

## The client application

The client is a plain WebFlux application that uses `RSocketRequester` to talk to the RSocket server. Declare a requester bean connected to `localhost:7000` over TCP:

```java
@Bean
public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
    return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .connect(TcpClientTransport.create("localhost", 7000))
            .block();
}
```

Create a generic `RestController` that forwards HTTP requests to RSocket routes:

```java
@Slf4j
@RequiredArgsConstructor
@RestController()
@RequestMapping("/posts")
class PostClientController {

    private final RSocketRequester requester;

    @GetMapping("")
    Flux<Post> all(@RequestParam(name = "title", required = false) String title) {
        if (StringUtils.hasText(title)) {
            return this.requester.route("posts.titleContains")
                    .data(title).retrieveFlux(Post.class);
        } else {
            return this.requester.route("posts.findAll")
                    .retrieveFlux(Post.class);
        }
    }

    @GetMapping("{id}")
    Mono<Post> findById(@PathVariable Integer id) {
        return this.requester.route("posts.findById." + id)
                .retrieveMono(Post.class);
    }

    @PostMapping("")
    Mono<Post> save(@RequestBody Post post) {
        return this.requester.route("posts.save")
                .data(post)
                .retrieveMono(Post.class);
    }

    @PutMapping("{id}")
    Mono<Post> update(@PathVariable Integer id, @RequestBody Post post) {
        return this.requester.route("posts.update." + id)
                .data(post)
                .retrieveMono(Post.class);
    }

    @DeleteMapping("{id}")
    Mono<Void> delete(@PathVariable Integer id) {
        return this.requester.route("posts.deleteById." + id).send();
    }
}
```

The payload object travels between the client and server as a normal Java bean. Because the RSocket starter auto-configures JSON codecs, the same `Post` model can be encoded and decoded without any extra serialization code.

This gives you a clean, reactive CRUD flow: the client sends `save`, `findById`, `findAll`, and `delete` requests over RSocket, while the server persists them through Spring Data R2DBC and streams the results back as reactive data.

See the complete example and tests in [rsocket-crud](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-crud).

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {
    private Integer id;
    private String title;
    private String content;
}
```

Start the client application (it serves on port 8080) and try the CRUD operations with `curl`:

```bash
# list all posts
curl http://localhost:8080/posts

# get a post by id
curl http://localhost:8080/posts/1

# save a new post
curl http://localhost:8080/posts -d "{\"title\":\"my save title\", \"content\":\"my content of my post\"}" -H "Content-Type:application/json"

# delete a post
curl http://localhost:8080/posts/4

# filter posts by keyword
curl http://localhost:8080/posts?title=one
```

## Testing

In the ported module the client code becomes tests: connect a requester to the server and verify the messaging routes with `StepVerifier` — no client application required.

```java
@SpringBootTest
public class IntegrationTests {

    static RSocketRequester requester;

    @BeforeAll
    public static void setup(@Autowired RSocketRequester.Builder builder) {
        requester = builder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connect(TcpClientTransport.create("localhost", 7000)).block();
    }

    @Test
    public void willLoadPosts() {
        requester.route("posts.findAll")
                .retrieveFlux(Post.class)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void testSaveAndGetAndDelete() {
        requester.route("posts.save")
                .data(Post.builder().title("test").content("content of test").build())
                .retrieveMono(Post.class)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("test", p.getTitle()))
                .verifyComplete();
    }
}
```

See the complete example and tests in [rsocket-crud](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-crud).
