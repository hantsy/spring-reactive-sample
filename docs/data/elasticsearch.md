# Accessing Elasticsearch with Spring Data Elasticsearch

Elasticsearch is a search engine that also works well as a document-oriented NoSQL store. In Spring, you can work with Elasticsearch in two common ways:

- by using Spring Data Elasticsearch repositories — backed by Spring Data's own `ReactiveElasticsearchClient`
- by working directly with the `ReactiveElasticsearchClient` that the repositories sit on

This chapter focuses on the reactive ecosystem and shows how to build a small, practical application using Spring WebFlux and Elasticsearch.

!!! important

    Unlike the other stores in this chapter, Elasticsearch is an exception: the official Elasticsearch Java Client SDK does **not** include a reactive client. Spring Data Elasticsearch fills this gap by providing its own `ReactiveElasticsearchClient` wrapper built on top of the low-level REST client. As a consequence, you cannot use a lightweight `spring-boot-starter-elasticsearch` alone — you must pull in the full Spring Data Elasticsearch starter to access the reactive client.

## Getting started

The quickest way to start is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../start/intro.md) chapter. Select **Spring WebFlux** and **Spring Data Elasticsearch**.

If you are adding Elasticsearch to an existing project, include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

With Spring Boot, the connection is usually configured automatically from `application.properties`:

```properties
spring.elasticsearch.uris=http://localhost:9200
```

## Defining the document model

Create a `Post` class that maps to an Elasticsearch index:

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class Post {

    @Id
    private String id;
    private String title;

    @Field("content")
    private String content;
}
```

The annotations in this example are straightforward:

1. `@Document` marks the class as an Elasticsearch document and names the target index.
2. `@Id` identifies the document ID field.
3. `@Field` maps a differently named field in the document.

## Creating a repository

Define a repository interface by extending `ReactiveElasticsearchRepository`:

```java
interface PostRepository extends ReactiveElasticsearchRepository<Post, String> {}
```

Once Spring creates the repository bean, you can inject it into controllers or services.

## Exposing a REST API

A small controller is enough to expose a reactive CRUD API:

```java
@RestController
@RequestMapping("/posts")
public class PostController {

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

Before starting the app, make sure Elasticsearch is running locally.

If you prefer Docker, start Elasticsearch with the Docker Compose file in the repository root:

```bash
docker-compose up elasticsearch
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

The `ReactiveElasticsearchClient` that Spring Data provides can be used directly, without repositories. Inject it and chain document operations in non-blocking Reactor streams.

### Implementing a repository with `ReactiveElasticsearchClient`

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

## Testing with Testcontainers

Define an `ElasticsearchContainer` bean with Spring Boot's `@ServiceConnection`:

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

You can explore the native-client example in [boot-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-elasticsearch), and the repository-based one in [boot-data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-elasticsearch).
