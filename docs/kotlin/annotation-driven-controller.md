# Annotation-Driven Controller

This page covers the annotation-driven Kotlin WebFlux controller — the Kotlin equivalent of the annotated `@RestController` samples from [Getting Started](../start/intro.md), keeping the Reactor types (`Mono`, `Flux`).

The samples referenced here are [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin) and [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin).

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

## Annotated controller

The [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin) sample mirrors the Java samples — annotation-driven controller, Reactor return types, Spring Data Mongo reactive repository:

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

The idiomatic Kotlin details: `val` constructor injection, expression bodies, and `it.copy(...)` on the immutable data class instead of setters.

## A Spring Boot variant

The [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin) sample uses the same controller with Spring Boot auto-configuration — the application class is minimal:

```kotlin
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    SpringApplication.run(DemoApplication::class.java, *args)
}
```
