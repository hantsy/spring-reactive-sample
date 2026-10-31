
# Events, Caching, and Scheduling

Spring's reactive support for application events, the caching abstraction, and task scheduling extends naturally to Kotlin coroutines. This page collects the coroutine-friendly patterns from the samples.

## Application events

An `@EventListener` can drive coroutine code. The listener method stays a regular function; inside it, use `runBlocking` when you need to block until coroutine work completes — from the [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) sample:

```kotlin
@Component
class DataInitializer(private val posts: PostRepository) {

    @EventListener(value = [ApplicationReadyEvent::class])
    fun init() {
        runBlocking {
            println(" print initial data...")
            posts.findAll()
        }
    }
}
```

This mirrors the Java pattern in [Application Events](../other/event.md) — publish with `ApplicationEventPublisher`, observe with `@EventListener` — with the handler body written in coroutine style. The same works in Spring Boot lifecycle hooks (`ApplicationRunner`, `CommandLineRunner`) via `runBlocking { ... }`.

## Scheduling

A `@Scheduled` method may be a `suspend` function that awaits a reactive pipeline with `awaitSingle` — from the [schedule](https://github.com/hantsy/spring6-sandbox/tree/main/schedule) sample of the spring6-sandbox repository:

```kotlin
@Component
class KotlinCounter {

    private val count = AtomicInteger(0)

    @Scheduled(fixedDelay = 5)
    suspend fun scheduled(): Void {
        return Mono.defer { Mono.just(count.incrementAndGet()) }
            .doOnNext { item: Int? -> log.debug("current count:{}", item) }
            .then()
            .awaitSingle()
    }

    suspend fun getInvocationCount(): Int {
        return count.get()
    }
}
```

The scheduler subscribes to the coroutine completion and waits for it before scheduling the next execution — the same semantics as the Java [Scheduling](../other/schedule.md) example, with `awaitSingle()` (from `kotlinx.coroutines.reactor`) bridging the `Mono` into the coroutine world.

The test asserts the counter advanced using `runTest`:

```kotlin
@SpringJUnitConfig(value = [TestConfig::class])
class KotlinCounterTest {

    @Autowired
    lateinit var counter: KotlinCounter

    @Test
    fun givenSleepBy100ms_whenGetInvocationCount_thenIsGreaterThanZero() = runTest {
        assertThat(counter.getInvocationCount()).isGreaterThan(0)
    }
}
```

## Caching

The [reactive caching](../other/cache.md) support added in Spring 6.1 covers Kotlin coroutines as well: `@Cacheable`/`@CachePut`/`@CacheEvict` apply to `suspend` functions and functions returning `Flow`, and the cache stores the completion rather than the publisher. Configure the cache manager exactly as in the Java chapter — `ConcurrentMapCacheManager`, async-mode `CaffeineCacheManager`, or `RedisCacheManager` — and annotate the repository or service methods:

```kotlin
@Cacheable(value = "posts", key = "#id")
suspend fun findById(id: UUID): Post? = ...
```

## JSON with kotlinx.serialization

Beyond Jackson, Spring Boot 4 adds a first-class starter for [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization): `spring-boot-starter-kotlinx-serialization-json` (plus its `-test` variant). A customizer tunes the `Json` instance:

```kotlin
@OptIn(ExperimentalSerializationApi::class)
@Configuration(proxyBeanMethods = false)
class CustomConfig {

    @Bean
    fun kotlinxSerializationJsonBuilderCustomizer(): KotlinxSerializationJsonBuilderCustomizer {
        return KotlinxSerializationJsonBuilderCustomizer { builder ->
            builder.apply {
                namingStrategy = JsonNamingStrategy.SnakeCase
                prettyPrint = true
                explicitNulls = false
                encodeDefaults = true
                ignoreUnknownKeys = false
            }
        }
    }
}
```

Annotate the DTOs with `@Serializable` and Spring encodes/decodes them on the HTTP boundary. See the [boot-kotlinx-serialization-json](https://github.com/hantsy/spring7-sandbox/tree/main/boot-kotlinx-serialization-json) sample and the [serialization guide](https://github.com/hantsy/spring7-sandbox/blob/main/docs/kotlinx-serialization.md) in the spring7-sandbox repository.
