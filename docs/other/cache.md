
# Caching

Since Spring 6.1, the caching abstraction supports reactive return types: a `@Cacheable` method may return `Mono` or `Flux`, and the cache stores the completion rather than the publisher. The repository ships three samples that differ only in the `CacheManager`: an in-memory `ConcurrentHashMap`, Caffeine, and Redis. All three combine the cache with a [Spring Data R2DBC](../data/r2dbc.md) backed `PostRepository` on PostgreSQL.

## Step 1: enable caching

```java
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
```

## Step 2: annotate the reactive repository

Apply the caching annotations to the repository methods — the reactive signatures work exactly like the blocking ones:

```java
public interface PostRepository extends R2dbcRepository<Post, UUID> {
    @Override
    @Cacheable(value = "posts", key = "#p0")
    Mono<Post> findById(UUID id);

    @Override
    @CachePut(value = "posts", key = "#result.id")
    Mono<Post> save(Post post);

    @Override
    @CacheEvict(value = "posts", key = "#p0")
    Mono<Void> deleteById(UUID id);

    @Override
    @CacheEvict(value = "posts", allEntries = true)
    Mono<Void> deleteAll();
}
```

## Step 3: choose the CacheManager

### Caffeine (async cache mode)

Caffeine supports native async caches — enable them with `setAsyncCacheMode(true)`:

```java
@Bean
@Override
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setAsyncCacheMode(true);
    return cacheManager;
}
```

Add the dependency:

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

### Redis

`RedisCacheManager` moves the cache across process boundaries. Declare the Redis connection factory and build the manager from it:

```java
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    @Override
    public CacheManager cacheManager() {
        return RedisCacheManager.builder().fromConnectionFactory(redisConnectionFactory)
                .build();
    }
}
```

Add the dependencies:

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>
```

!!! note

    If both `cacheManager` and `cacheResolver` are set, the cache manager is ignored — configure one or the other.

## Verify

The sample test performs the same `findById` twice and observes the cache interceptor in the logs (the `org.springframework.cache` category is set to `TRACE` in `logback.xml`): the second call hits the cache instead of the database, and the eviction annotations clear the entries on save/delete.

To run the application, start PostgreSQL (and Redis for the Redis variant):

```bash
docker-compose up postgres redis
```

For the complete codes, check [cache](https://github.com/hantsy/spring-reactive-sample/tree/master/cache), [cache-caffeine](https://github.com/hantsy/spring-reactive-sample/tree/master/cache-caffeine), and [cache-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/cache-redis).
