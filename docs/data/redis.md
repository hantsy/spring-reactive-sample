# Accessing Redis with Spring Data Redis

Redis is a fast, lightweight data store that is often used for caching, counters, queues, and small message bursts. In a Spring WebFlux application, it fits naturally alongside Reactor streams because both are designed around async, non-blocking work.

You can work with Redis in two common ways:

- through Spring Data Redis, using `ReactiveRedisConnectionFactory`, `ReactiveRedisTemplate`, and reactive pub/sub support
- directly through the Lettuce client, where you compose driver-level operations with Reactor types

This example keeps things practical: we wire up a tiny reactive API, persist a few documents, and show how the code looks in a real application rather than in a theoretical abstraction.

## Getting started

The quickest way to start is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../start/intro.md) chapter. Select **Spring WebFlux** and **Spring Data Redis (Reactive)**.

If you are adding Redis to an existing project, include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

Spring Boot auto-configures a `ReactiveRedisConnectionFactory` for you.

If you are building a plain Spring project without Boot auto-configuration, use the following dependencies instead — Lettuce is the required driver for reactive support:

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

In that case, you also need a configuration class to enable reactive Redis support:

```java
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public ReactiveRedisTemplate<String, Post> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<String, Post>(
            factory,
            RedisSerializationContext.fromSerializer(new JacksonJsonRedisSerializer(Post.class))
        );
    }
}
```

`LettuceConnectionFactory` implements `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` interfaces — when a `LettuceConnectionFactory` is declared, both are registered as Spring beans.

You can explore this setup in [data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis).

With Spring Boot, the connection is usually configured automatically from `application.properties`:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## Defining the data model

Create a `Post` class to present a Redis hash:

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("posts")
class Post {

    @Id
    private String id;
    private String title;
    private String content;
}
```

The annotations in this example are straightforward:

1. `@RedisHash` marks the class as a Redis hash stored under the `posts` collection.
2. `@Id` identifies the identifier field.

## Creating a repository

Spring Data Redis does not provide a reactive variant of a repository interface like Mongo does. Instead, compose the data access on `ReactiveRedisOperations`:

```java
@Repository
@RequiredArgsConstructor
class PostRepository {

    private final ReactiveRedisOperations<String, Post> reactiveRedisOperations;

    public Flux<Post> findAll(){
        return this.reactiveRedisOperations.opsForList().range("posts", 0, -1);
    }

    public Mono<Post> findById(String id) {
        return this.findAll().filter(p -> p.getId().equals(id)).last();
    }

    public Mono<Long> save(Post post){
        return this.reactiveRedisOperations.opsForList().rightPush("posts", post);
    }

    public Mono<Boolean> deleteAll() {
        return this.reactiveRedisOperations.opsForList().delete("posts");
    }
}
```

Once the bean is created, you can inject it into controllers or services.

## Exposing a REST API

A small controller is enough to expose a reactive read API:

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
}
```

The write path is covered by the [Redis Messaging](#redis-messaging) section below — new posts arrive as pub/sub messages rather than direct saves.

## Seeding sample data

You can use an `@EventListener` on `ContextRefreshedEvent` to insert a few sample records when the application starts:

```java
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {

    private final PostRepository posts;

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.initPosts();
        log.info("done data initialization  ...");
    }

    private void initPosts() {
        this.posts.deleteAll()
                .thenMany(
                        Flux.just("Post one", "Post two")
                                .flatMap(title -> this.posts.save(
                                        Post.builder().id(UUID.randomUUID().toString())
                                                .title(title).content("content of " + title).build()))
                )
                .subscribe(null, null, () -> log.info("done posts initialization..."));
    }
}
```

## Running the application

Before starting the app, make sure Redis is running locally.

If you prefer Docker, it is easy to start Redis with the Docker Compose file in the repository root:

```bash
docker-compose up redis
```

Then launch the application:

```bash
mvn spring-boot:run
```

You can test the API with `curl`:

```bash
curl -v http://localhost:8080/posts
```

## Redis Messaging

Redis is well known as a key-value database, and it is also a lightweight messaging broker.

Declare a `ReactiveRedisMessageListenerContainer` bean to receive messages from Redis pub/sub channels reactively:

```java
@Bean
public ReactiveRedisMessageListenerContainer redisMessageListenerContainer(PostRepository posts, ReactiveRedisConnectionFactory connectionFactory) {
    ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(connectionFactory);
    ObjectMapper objectMapper = new JsonMapper();
    container.receive(ChannelTopic.of("posts"))
        .map(p -> p.getMessage())
        .map(m -> {
            try {
                Post post = objectMapper.readValue(m, Post.class);
                post.setId(UUID.randomUUID().toString());
                return post;
            } catch (JacksonException e) {
                return null;
            }
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException()))
        .flatMap(p -> posts.save(p))
        .subscribe(c -> log.info(" count:" + c), null, () -> log.info("saving post."));
    return container;
}
```

The `container.receive(ChannelTopic.of("posts"))` call subscribes to the `posts` channel and returns a `Flux` of messages, so the processing pipeline is plain Reactor composition.

Send a message using the `ReactiveRedisOperations` bean:

```java
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
class PostController {

    //...
    @PostMapping("")
    public Mono<Long> save(@RequestBody Post post) {
        return this.reactiveRedisOperations.convertAndSend("posts", post);
    }
}
```

You can explore the messaging example in [data-redis-message](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis-message). For a wider view of reactive message channels — Redis pub/sub, Spring Integration `FluxMessageChannel`, and Reactor `Sinks` side by side — see [Reactive Message Channels](../other/integration/message-channel.md).

## Working with the native client

Beside the connection factory, you can inject a `ReactiveRedisConnectionFactory` bean and get a `ReactiveConnection` for lower-level Redis operations.

```java
@Inject ReactiveRedisConnectionFactory factory;

ReactiveRedisConnection conn = factory.getReactiveConnection();
```

`ReactiveConnection` provides reactive methods for the Redis command groups. For example, create a favorites list for posts:

```java
conn.setCommands()
    .sAdd(
        ByteBuffer.wrap("users:user:favorites".getBytes()),
        this.posts.findAll()
            .stream()
            .map(p -> p.getId().getBytes())
            .map(ByteBuffer::wrap)
            .collect(Collectors.toList())
    )
    .log()
    .subscribe(null, null, () -> log.info("added favorites..."));
```

And show the favorites in a controller:

```java
@RestController
@RequestMapping("/favorites")
class FavoriteController {

    private final ReactiveRedisConnectionFactory factory;

    public FavoriteController(ReactiveRedisConnectionFactory factory) {
        this.factory = factory;
    }

    @GetMapping("")
    public Mono<List<String>> all() {
        return this.factory.getReactiveConnection()
                .setCommands()
                .sMembers(ByteBuffer.wrap("users:user:favorites".getBytes()))
                .map(FavoriteController::toString)
                .collectList();
    }

    private static String toString(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes);
    }
}
```

## Testing with Testcontainers

Define a `RedisContainer` bean and map its port for the test context. From the [cache-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/cache-redis) sample:

```java
private static final RedisContainer REDIS = new RedisContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));

TestPropertyValues.of(
        "redis.host=" + REDIS.getHost(),
        "redis.port=" + REDIS.getFirstMappedPort()
).applyTo(context.getEnvironment());
```

You can explore the template-based example in [boot-data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-redis) and [boot-data-redis-template](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-redis-template), and the reactive caching example in [cache-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/cache-redis) — see [Caching](../other/cache.md) for the cache manager configuration.
