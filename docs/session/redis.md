
# Redis-backed sessions

The [spring-session-data-redis](https://spring.io/projects/spring-session) module stores sessions in Redis through a reactive Redis connection, so sessions survive application restarts and are shared between all instances that point at the same Redis.

## Step 1: add the dependencies

```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>
```

Lettuce is the required driver for reactive Redis support (see [Spring Data Redis](../data/redis.md)).

## Step 2: configure the Redis connection factory

```java
@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }
}
```

The default `LettuceConnectionFactory` connects to `localhost:6379`. To start a Redis server for local development, use the `docker-compose.yml` in the repository root:

```bash
docker-compose up redis
```

## Step 3: enable Redis web session

```java
@Configuration
@EnableRedisWebSession
public class SessionConfig {
}
```

`@EnableRedisWebSession` registers a reactive session repository backed by Redis automatically — no explicit `ReactiveSessionRepository` bean is needed. The controller and template code is identical to the [in-memory example](in-memory.md#step-3-use-websession-in-a-controller).

## Verify

Start the application and submit the attribute form at http://localhost:8080. Then inspect Redis directly:

```
redis-cli keys "spring:session*"
```

You will find the session keyed under the `spring:session` prefix, storing the attributes and metadata.

For the complete codes, check [spring-reactive-sample/session-data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/session-data-redis).
