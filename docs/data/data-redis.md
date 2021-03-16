---
sort: 5
---


# Spring Data Redis

Spring Data Redis provides a reactive variant of `RedisConnectionFactory` aka `ReactiveRedisConnectionFactory` which return a `ReactiveConnection`.

## Getting Started

Follow the the [Getting Started](./start) part to create a freestyle or Spring Boot based project skeleton.

For none Spring Boot project, add the following dependencies to the *pom.xml*.

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

>**NOTE**: You have to use `lettuce` as Redis driver to get reactive support in `spring-data-redis`, and add `commons-pool2` to support Redis connection pool.

Create a `@Configuration` class to configure Mongo and enable Reactive support for Redis.

```java
@Configuration
@Slf4j
public class RedisConfig {


    @Bean
    public ReactiveRedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

//    @Bean
//    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {
//
//        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//            .useSsl().and()
//            .commandTimeout(Duration.ofSeconds(2))
//            .shutdownTimeout(Duration.ZERO)
//            .build();
//
//        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379), clientConfig);
//    }

    @Bean
    public ReactiveRedisTemplate<String, Post> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<String, Post>(
            factory,
            RedisSerializationContext.fromSerializer(new Jackson2JsonRedisSerializer(Post.class))
        );
    }
}	
```

`LettuceConnectionFactory` implements `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` interfaces, when a `LettuceConnectionFactory` is declared, both `RedisConnectionFactory` and `ReactiveRedisConnectionFactory` are registered as Spring beans. 

In your components, you can inject a `ReactiveRedisConnectionFactory` bean to get a `ReactiveConnection1`.

```java
@Inject ReactiveRedisConnectionFactory factory;

ReactiveRedisConnection conn = factory.getReactiveConnection();
```

`ReactiveConnection` provides some reactive methods for Redis operations.

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

For the complete codes, check [spring-reactive-sample/data-redis](https://github.com/hantsy/spring-reactive-sample/blob/master/data-redis).
 
For Spring Boot applications, the configuration can be simplified. Just add `spring-boot-starter-data-redis-reactive` into the project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

Spring boot provides auto-configuration for Redis, and registers `ReactiveRedisConnectionFactory` for you automatically.

For the complete codes, check [spring-reactive-sample/boot-data-redis](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-redis).


## ReactiveRedisTemplate

Beside the `ReactiveRedisConnectionFactory`, Spring Data Redis also provides a variant for `RedisTemplate`.

Let's try to add some sample via a generic Repository interface. Create a `Post` class to present a Redis hash data, add `@RedisHash("posts")` to `Post` class.

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

Create a `PostRepository` and use `ReactiveRedisOperations` to access the Redis database.

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
## Redis Messaging

Redis is well known as a key value database, it is also famous for a light-weight messaging broker. 

Declare a `ReactiveRedisMessageListenerContainer` bean to receive messaging from Redis.

```java
@Bean
public ReactiveRedisMessageListenerContainer redisMessageListenerContainer(PostRepository posts, ReactiveRedisConnectionFactory connectionFactory) {
	ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(connectionFactory);
	ObjectMapper objectMapper = new ObjectMapper();
	container.receive(ChannelTopic.of("posts"))
		.map(p->p.getMessage())
		.map(m -> {
			try {
				Post post= objectMapper.readValue(m, Post.class);
				post.setId(UUID.randomUUID().toString());
				return post;
			} catch (IOException e) {
				return null;
			}
		})
		.switchIfEmpty(Mono.error(new IllegalArgumentException()))
		.flatMap(p-> posts.save(p))
		.subscribe(c-> log.info(" count:" + c), null , () -> log.info("saving post."));
	return container;
}
```	

Send a message using `ReactiveRedisOperations` bean.

```java
@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
class PostController {

    //...
    @PostMapping("")
    public Mono<Long> save(@RequestBody Post post) {
        return this.reactiveRedisOperations.convertAndSend("posts", post );
    }
	
}	
```

For the complete codes, check [spring-reactive-sample/data-redis-message](https://github.com/hantsy/spring-reactive-sample/blob/master/data-redis-message).
