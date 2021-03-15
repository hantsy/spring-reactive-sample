---
sort: 5
---


# Spring Data Redis

Spring Data Redis provides a reactive variant of `RedisConnectionFactory` aka `ReactiveRedisConnectionFactory` which return a `ReactiveConnection`.

Add the following into your project dependencies.

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

**NOTE**: You have to use `lettuce` as redis driver to get reactive support in `spring-data-redis`, and add `commons-pool2` to support Redis connection pool.

Create a `@Configuration` class to configure Mongo and enable Reactive support for Redis.

```java
@EnableRedisRepositories
public class RedisConfig {

    @Autowired
    RedisConnectionFactory factory;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }
    
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }

    @PreDestroy
    public void flushTestDb() {
        factory.getConnection().flushDb();
    }

}
```

`LettuceConnectionFactory` implements `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` interfaces, when a `LettuceConnectionFactory` is declared, `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` are also registered as beans. 



In your beans, you can inject a `ReactiveRedisConnectionFactory` and get a reactive connection.

```java
@Inject ReactiveRedisConnectionFactory factory;

ReactiveRedisConnection conn = factory.getReactiveConnection();
```

`ReactiveConnection` provides some reactive methods for redis operations.

For example, create a favorites list for posts.

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
	.subscribe(null, null, ()-> log.info("added favirates..."));
```

And show my favorites in the controller.

```java
@RestController()
@RequestMapping(value = "/favorites")
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

## Spring Boot

For Spring Boot applications, the configuration can be simplified. Just add `spring-boot-starter-data-redis-reactive` into the project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

Spring boot provides auto-configuration for redis, and registers `ReactiveRedisConnectionFactory` for you automatically.

## Data Initialization

Declare `Post` as a redis hash data, add `@RedisHash("posts")` to `Post` POJO.

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

Let's have a look at the `PostRepository`.

```java
interface PostRepository extends KeyValueRepository<Post, String> {

    @Override
    public List<Post> findAll();
}
```

`KeyValueRepository` is from `spring-data-keyvalue`, which is a generic Map based Repository implementation.

```java
private void initPosts() {
	this.posts.deleteAll();
	Stream.of("Post one", "Post two").forEach(
		title -> this.posts.save(Post.builder().id(UUID.randomUUID().toString()).title(title).content("content of " + title).build())
	);
}
```

**NOTE**: Unlike Spring Data Mongo, Spring Data Redis does not provides a variant for `RedisTemplate` and `Repository`.

