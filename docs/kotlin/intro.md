
# Kotlin

Kotlin is a first-class citizen in the Spring ecosystem. Spring Framework provides dedicated Kotlin DSLs for bean definitions and functional routing (`BeanRegistrarDsl`, `coRouter`), Spring Data supports coroutine-based repositories (`CoroutineCrudRepository`), and the web layer accepts `suspend` functions and `Flow` wherever a `Publisher` is expected.

This chapter covers Kotlin support end to end, based on the runnable samples in this repository and the companion [spring7-sandbox](https://github.com/hantsy/spring7-sandbox) / [spring6-sandbox](https://github.com/hantsy/spring6-sandbox) repositories.

## Chapter roadmap

1. [Coroutines](coroutines.md) — `suspend` controllers, `Flow`, `CoroutineCrudRepository`, and testing with MockK.
2. [Kotlin DSLs](dsl.md) — `BeanRegistrarDsl` bean definitions, `coRouter { }` routes, and suspend handlers.
3. [CoWebFilter](co-web-filter.md) — the coroutine variant of `WebFilter`.
4. [Events, caching, and scheduling](misc.md) — reactive application events, coroutine-friendly caching, and `suspend` scheduled tasks.

## Setting up a Kotlin WebFlux project

The essential dependencies (versions are managed by the Spring Boot parent):

```xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-reactor</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor.kotlin</groupId>
    <artifactId>reactor-kotlin-extensions</artifactId>
</dependency>
<dependency>
    <groupId>tools.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
</dependency>
```

The Kotlin compiler needs the *allopen* plugin so Spring classes and `@Component`-annotated classes can be proxied:

```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <configuration>
        <args>
            <arg>-Xjsr305=strict</arg>
        </args>
        <compilerPlugins>
            <plugin>spring</plugin>
        </compilerPlugins>
        <jvmTarget>21</jvmTarget>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-allopen</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

`-Xjsr305=strict` makes Spring's nullability annotations strict, so the Kotlin compiler enforces null-safety at the Spring API boundaries.

## Annotation-driven Kotlin

The [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) sample mirrors the Java samples — annotation-driven controller, Spring Data Mongo repository — with idiomatic Kotlin:

```kotlin
@RestController
@RequestMapping(value = ["/posts"])
class PostController(val posts: PostRepository) {

    @GetMapping("")
    fun all(): Flux<Post> = this.posts.findAll()

    @PostMapping("")
    fun create(@RequestBody post: Post): Mono<Post> = this.posts.save(post)

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: String): Mono<Post> = this.posts.findById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: String, @RequestBody post: Post): Mono<Post> {
        return this.posts.findById(id)
                .map { it.copy(title = post.title, content = post.content) }
                .flatMap { this.posts.save(it) }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String): Mono<Void> = this.posts.deleteById(id)
}
```

This style mixes freely with Reactor types. The next page replaces `Mono`/`Flux` with coroutines altogether — see [Coroutines](coroutines.md).
