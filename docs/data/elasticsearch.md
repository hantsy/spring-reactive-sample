# Accessing Elasticsearch with Spring Data Elasticsearch

Elasticsearch is a search engine that also works well as a document-oriented NoSQL store. Spring Data Elasticsearch adds reactive support through its own `ReactiveElasticsearchClient` and `ReactiveElasticsearchRepository`.

This page follows the flow used across the chapter: generate a project as in [Getting Started](../start/intro.md), add the dependency, try the native client, then move to the Spring Data abstractions.

## Getting started

Generate a project skeleton from [Spring Initializr](https://start.spring.io) as described in the [Getting Started](../start/intro.md) chapter — choose **Spring WebFlux** and **Spring Data Elasticsearch** — or add the starter to an existing `pom.xml`:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

Start an Elasticsearch server with the `docker-compose.yml` in the repository root:

```bash
docker-compose up elasticsearch
```

## Working with the native client

The synchronous starter `spring-boot-starter-elasticsearch` configures a blocking `ElasticsearchClient`. Spring Data provides the `ReactiveElasticsearchClient` to execute indexing and search operations reactively using Project Reactor types.

!!! important

    Unlike the other stores in this chapter, Elasticsearch is an exception: the official Elasticsearch Java Client SDK does **not** include a reactive client. Spring Data Elasticsearch fills this gap by providing its own `ReactiveElasticsearchClient` wrapper built on top of the low-level REST client. As a consequence, you cannot use a lightweight `spring-boot-starter-elasticsearch` alone — you must pull in the full Spring Data Elasticsearch starter to access the reactive client.

### Project setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### Implementing a thin repository

Inject the `ReactiveElasticsearchClient` provided by Spring Data and chain document operations in non-blocking Reactor streams:

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

## Spring Data Elasticsearch

### Entity and repository

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
class Post {

    @Id
    private String id;
    private String title;

    @Field("content")
    private String content;

}
```

Create `PostRepository` interface.

```java
interface PostRepository extends ReactiveElasticsearchRepository<Post, String> {
}
```

### Seeding sample data

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

To run the application, you have to serve a running ElasticSearch server firstly.

```bash
docker-compose up elasticsearch
```

## All samples

| Sample | Description |
| :--- | :--- |
| [boot-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-elasticsearch) | Native `ReactiveElasticsearchClient` (thin client) with Testcontainers |
| [boot-data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-elasticsearch) | Spring Boot starter with `ReactiveElasticsearchRepository` |
